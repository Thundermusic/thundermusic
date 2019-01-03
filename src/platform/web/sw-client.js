import { CORS_PROXY } from "./cors";

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

export function swDownload(getMeta) {
  return async (music, meta, progressFn) => {
    if (
      "serviceWorker" in navigator &&
      (await navigator.serviceWorker.getRegistration())
    ) {
      await waitForController();

      const url = `/musics/${music.id}`;

      const { data: hasDownloaded } = await sendMessage({
        type: "hasDownloaded",
        url
      });

      if (hasDownloaded) {
        return url;
      } else {
        return fetchToFile(await (meta || getMeta(music)), music, progressFn);
      }
    } else {
      console.warn("Service Worker not enabled");
      const { url } = await (meta || getMeta(music));
      progressFn(1);
      return url;
    }
  };
}

export async function fetchToFile(
  { url, size },
  { id /* title, thumbnail */ },
  progressFn
) {
  /*const registration = await navigator.serviceWorker.ready;

  if (
    registration.backgroundFetch &&
    !(await registration.backgroundFetch.get(id))
  ) {
    await registration.backgroundFetch.fetch(id, [request], {
      title,
      downloadTotal: format.clen,
      icons: [
        {
          src: thumbnail
        }
      ]
    });
  } else {
    console.log("Background Fetch not available");
  }*/

  // We need cors headers to show a progress bar :)
  const { data, port } = await sendMessage({
    type: "downloadTo",
    from: CORS_PROXY + url,
    to: `/musics/${id}`,
    size
  });

  port.onmessage = ({ data }) => progressFn(data);

  return data;
}

export async function cleanupMusic(music) {
  if (
    "serviceWorker" in navigator &&
    (await navigator.serviceWorker.getRegistration())
  ) {
    await waitForController();
    await sendMessage({
      type: "delete",
      url: await music.url
    });
  }
}
