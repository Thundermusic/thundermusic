import { swDownload } from "../sw-client";
import { search } from "./api";
import { parse } from "querystring";
import FORMATS from "./formats";
import { getTokens, decipherFormat } from "./sig";

const CORS_PROXY = "https://cors-anywhere.herokuapp.com/";

const VIDEO_EURL = "https://youtube.googleapis.com/v/";
const INFO_URL = "https://www.youtube.com/get_video_info/";

function between(haystack, left, right) {
  const lPos = haystack.indexOf(left);
  if (lPos === -1) {
    return "";
  }
  haystack = haystack.slice(lPos + left.length);
  const rPos = haystack.indexOf(right);
  if (rPos === -1) {
    return "";
  }
  haystack = haystack.slice(0, rPos);
  return haystack;
}

function addFormatMeta(format) {
  const meta = FORMATS[format.itag];
  for (let key in meta) {
    format[key] = meta[key];
  }

  format.live = /\/source\/yt_live_broadcast\//.test(format.url);
  format.isHLS = /\/manifest\/hls_variant\//.test(format.url);
  format.isDashMPD = /\/manifest\/dash\//.test(format.url);
  return format;
}

function parseFormats(info) {
  let formats = [];
  if (info.url_encoded_fmt_stream_map) {
    formats = formats.concat(info.url_encoded_fmt_stream_map.split(","));
  }
  if (info.adaptive_fmts) {
    formats = formats.concat(info.adaptive_fmts.split(","));
  }

  formats = formats.map(format => parse(format));
  delete info.url_encoded_fmt_stream_map;
  delete info.adaptive_fmts;

  return formats.map(addFormatMeta);
}

async function getFormat(id) {
  const body = await fetch(
    `${CORS_PROXY}https://www.youtube.com/embed/${id}?hl=en`
  ).then(res => res.text());
  const config = JSON.parse(
    between(body, "yt.setConfig({'PLAYER_CONFIG': ", "})")
  );
  const html5player = config.assets.js;

  // Fetch early
  const tokens = getTokens(
    `${CORS_PROXY}https://www.youtube.com${html5player}`
  );

  const infos = parse(
    await fetch(
      `${CORS_PROXY}${INFO_URL}?video_id=${id}&eurl=${VIDEO_EURL +
        id}&ps=default&gl=US&hl=en&sts=${config.sts}`
    ).then(res => res.text())
  );

  const formats = parseFormats(infos);

  // TODO: support adaptive formats (HSLS & Dash) like ytdl-core
  const downloadable = formats.filter(
    ({ live, isHLS, isDashMPD }) => !live && !isHLS && !isDashMPD
  );

  const audioOnly = downloadable
    .filter(({ bitrate, audioBitrate }) => !bitrate && audioBitrate)
    .sort(
      ({ audioBitrate: aAudioBitrate }, { audioBitrate: bAudioBitrate }) =>
        bAudioBitrate - aAudioBitrate
    );

  const [toDownload] = audioOnly.concat(downloadable);

  decipherFormat(toDownload, await tokens);

  return toDownload;
}

const MAX_RETRIES = 3;

function getMeta(song, nRetry = 0) {
  return getFormat(song.id).then(
    ({ url, clen }) => ({
      url,
      size: +(clen || new URL(url).searchParams.get("clen"))
    }),
    err => {
      if (nRetry < MAX_RETRIES) {
        console.warn(
          `Failed to get youtube metadata, retring ${nRetry +
            1}/${MAX_RETRIES}`,
          err
        );
        return getMeta(song, nRetry + 1);
      } else throw err;
    }
  );
}

export default {
  name: "Youtube",
  icon: require("../../../assets/youtube_icon.svg"),
  search,
  getMeta,
  download: swDownload(getMeta)
};
