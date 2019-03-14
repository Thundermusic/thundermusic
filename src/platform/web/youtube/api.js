const YOUTUBE_API_KEY = process.env.VUE_APP_YOUTUBE_API_KEY;

const API_URL = "https://www.googleapis.com/youtube/v3";

function convertTime(duration) {
  const hours = duration.match(/(\d+)H/);
  const minutes = duration.match(/(\d+)M/);
  const seconds = duration.match(/(\d+)S/);

  const dur = [hours, minutes, seconds].map(e => (e ? e[1] : ""));
  if (!dur[0]) dur.splice(0, 1);

  return dur.map(e => e.padStart(2, "0")).join(":");
}

function mapSnippets(items) {
  return items.map(
    ({
      id: { videoId: idVideoId },
      snippet: {
        title,
        channelTitle: channel,
        thumbnails: { high: { url: thumbnail } = {} } = {},
        resourceId: { videoId: rIdVideoId } = {}
      }
    }) => ({
      id: idVideoId || rIdVideoId,
      title,
      artist: channel,
      thumbnail,
      duration: null,
      youtube: {
        videoId: idVideoId || rIdVideoId
      }
    })
  );
}

export async function search(query) {
  const { items } = await fetch(
    `${API_URL}/search?part=snippet&q=${encodeURIComponent(
      query
    )}&maxResults=50&type=video&key=${YOUTUBE_API_KEY}`
  ).then(res => res.json());
  const results = mapSnippets(items);
  return addDuration(results);
}

async function addDuration(results) {
  const ids = results.map(({ youtube: { videoId } }) => videoId);
  const { items } = await fetch(
    `${API_URL}/videos?part=contentDetails&id=${ids.join(
      ","
    )}&key=${YOUTUBE_API_KEY}`
  ).then(res => res.json());

  items.forEach(
    ({ contentDetails: { duration } }, i) =>
      (results[i].duration = convertTime(duration))
  );

  return results;
}

export async function getPlaylistContent(id) {
  const allResults = [];
  let pageToken = undefined;
  do {
    const { items, nextPageToken } = await fetch(
      `${API_URL}/playlistItems?part=snippet&playlistId=${id}&maxResults=50&key=${YOUTUBE_API_KEY}${
        pageToken ? `&pageToken=${pageToken}` : ""
      }`
    ).then(res => res.json());

    allResults.push(addDuration(mapSnippets(items)));
    pageToken = nextPageToken;
  } while (pageToken);

  const results = await Promise.all(allResults);

  return results.reduce((a, b) => a.concat(b), []);
}

export async function getPlaylistInfos(id) {
  const {
    items: [{ snippet }]
  } = await fetch(
    `${API_URL}/playlists?part=snippet&id=${id}&key=${YOUTUBE_API_KEY}`
  ).then(res => res.json());

  return snippet;
}
