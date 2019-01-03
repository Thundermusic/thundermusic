import { swDownload } from "./sw-client";

const CLIENT_ID = "yKErPEAPC9QCeJnXv3FNzzRKaEqRZua6";
const CORS_PROXY = "https://cors-anywhere.herokuapp.com/";
const API_URL = CORS_PROXY + "https://api-v2.soundcloud.com";

function formatDuration(duration) {
  const inSeconds = duration / 1000;
  const hours = Math.floor(inSeconds / 3600);
  const minutes = Math.floor((inSeconds % 3600) / 60);
  const seconds = Math.floor(inSeconds % 60);

  const dur = [hours, minutes, seconds];
  if (!dur[0]) dur.splice(0, 1);

  return dur.map(e => e.toString().padStart(2, "0")).join(":");
}

async function search(query) {
  const { collection } = await fetch(
    `${API_URL}/search/tracks?q=${encodeURIComponent(
      query
    )}&client_id=${CLIENT_ID}&limit=50&offset=0`
  ).then(res => res.json());

  return collection.map(
    ({
      urn: id,
      title,
      user: { full_name: artist },
      artwork_url: thumbnail,
      duration,
      media: { transcodings }
    }) => ({
      id,
      title,
      artist,
      thumbnail,
      duration: formatDuration(duration),
      soundcloud: {
        transcodings
      }
    })
  );
}

async function getMeta(music) {
  const { transcodings } = music.soundcloud;

  const transcoding = transcodings.find(
    ({ format }) => format.protocol === "progressive"
  );
  const { url } = await fetch(
    `${CORS_PROXY}${transcoding.url}?client_id=${CLIENT_ID}`
  ).then(res => res.json());

  return { url };
}
export default {
  name: "Soundcloud",
  icon: require("../../assets/soundcloud_icon.svg"),
  search,
  getMeta,
  download: swDownload(getMeta)
};
