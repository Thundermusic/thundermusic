import { parse } from "querystring";
import FORMATS from "./formats";
import { getTokens, decipherFormat } from "./sig";
import url from "url";

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

export async function getFormat(id) {
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

function sendMessage(message) {
  const channel = new MessageChannel();

  navigator.serviceWorker.controller.postMessage(message, [channel.port1]);

  return new Promise(
    resolve =>
      (channel.port2.onmessage = ({ data }) =>
        resolve({ data, port: channel.port2 }))
  );
}

function waitForController() {
  return new Promise(resolve => {
    if (navigator.serviceWorker.controller) resolve();
    else {
      const handler = () => {
        resolve();
        navigator.serviceWorker.removeEventListener(
          "controllerchange",
          handler
        );
      };

      navigator.serviceWorker.addEventListener("controllerchange", handler);
    }
  });
}

export async function download(song, progressFn) {
  if (
    "serviceWorker" in navigator &&
    (await navigator.serviceWorker.getRegistration())
  ) {
    await waitForController();

    const url = `/musics/${song.id}`;

    const { data: hasDownloaded } = await sendMessage({
      type: "hasDownloaded",
      url
    });

    if (hasDownloaded) {
      return url;
    } else {
      return fetchToFile(await getFormat(song.id), song, progressFn);
    }
  } else {
    console.warn("Service Worker not enabled");
    const { url } = await getFormat(song.id);
    progressFn(1);
    return url;
  }
}

export async function fetchToFile(
  format,
  { id /* title, thumbnail */ },
  progressFn
) {
  const request = CORS_PROXY + format.url;

  /*const registration = await navigator.serviceWorker.ready;

	if (registration.backgroundFetch && ! (await registration.backgroundFetch.get(id))) {
		await registration.backgroundFetch.fetch(id, [
			request
		], {
			title,
			downloadTotal: format.clen,
			icons: [{
				src: thumbnail
			}]
		});
	} else {
		console.log("Background Fetch not available")
	}*/

  const { data, port } = await sendMessage({
    type: "downloadTo",
    from: request,
    to: `/musics/${id}`,
    size: +(format.clen || url.parse(request, true).query.clen)
  });

  port.onmessage = ({ data }) => progressFn(data);

  return data;
}
