workbox.clientsClaim();
workbox.skipWaiting();

workbox.routing.registerRoute(
  new RegExp("^https://fonts.(?:googleapis|gstatic).com/(.*)"),
  workbox.strategies.cacheFirst()
);

workbox.routing.registerNavigationRoute("/index.html", {
  blacklist: [/img/, /manifest\.json/]
});

workbox.precaching.precacheAndRoute(self.__precacheManifest);

const DOWNLOADING = new Map();

self.addEventListener("message", event => {
  switch (event.data.type) {
    case "downloadTo":
      event.waitUntil(
        (async () => {
          const { to, from, size } = event.data;
          const cache = await caches.open("musics");
          const res = monitorProgress(await fetch(from), event.ports[0], size);
          DOWNLOADING.set(to, res);
          event.ports[0].postMessage(to);
          await cache.put(to, res.clone());
          event.ports[0].postMessage(1);
          DOWNLOADING.delete(to);
        })()
      );
      break;
    case "hasDownloaded":
      event.waitUntil(
        (async () => {
          const { url } = event.data;
          const cache = await caches.open("musics");
          event.ports[0].postMessage(!!(await cache.match(url)));
        })()
      );
      break;
    case "delete":
      event.waitUntil(
        (async () => {
          const { url } = event.data;
          const cache = await caches.open("musics");
          await cache.delete(url, {
            ignoreMethod: true,
            ignoreSearch: true,
            ignoreVary: true
          });
          event.ports[0].postMessage(true);
        })()
      );
  }
});

function monitorProgress(
  response,
  port,
  size = +response.headers.get("content-length")
) {
  if (!response.body) {
    console.warn(
      "ReadableStream is not yet supported in this browser. See https://developer.mozilla.org/en-US/docs/Web/API/ReadableStream"
    );
    return response;
  }

  if (!response.ok) {
    // HTTP error code response
    return response;
  }

  if (!isFinite(size)) {
    return response;
  }

  const reader = response.body.getReader();

  return new Response(
    new ReadableStream({
      async start(controller) {
        try {
          let loaded = 0;
          for (;;) {
            const { done, value } = await reader.read();
            if (done) break;
            controller.enqueue(value);
            loaded += value.byteLength;
            port.postMessage(loaded / size);
          }
          controller.close();
        } catch (e) {
          controller.error(e);
        }
      },

      cancel(reason) {
        reader.cancel(reason);
      }
    })
  );
}

workbox.routing.registerRoute(new RegExp("/musics/.+"), async ({ event }) => {
  const url = event.request.url;
  const urlEnd = url.slice(url.indexOf("/musics/"));

  if (DOWNLOADING.has(urlEnd)) {
    // TODO Seek for download
    return DOWNLOADING.get(urlEnd).clone();
  }

  const cache = await caches.open("musics");
  const res = await cache.match(event.request);

  if (event.request.headers.get("range")) {
    const [, pos] = /^bytes=(\d+)-$/g.exec(event.request.headers.get("range"));
    console.log(
      "Range request for",
      event.request.url,
      ", starting position:",
      pos
    );
    const ab = await res.arrayBuffer();
    return new Response(ab.slice(+pos), {
      status: 206,
      statusText: "Partial Content",
      headers: [
        ["Content-Range", `bytes ${pos}-${ab.byteLength - 1}/${ab.byteLength}`]
      ]
    });
  } else return res;
});

self.addEventListener("backgroundfetchsuccess", event => {
  console.log("SUCCESS", event);
});

self.addEventListener("backgroundfetchfail", event => {
  console.log("FAIL", event);
});

self.addEventListener("backgroundfetchabort", event => {
  console.log("ABORT", event);
});

self.addEventListener("backgroundfetchclick", event => {
  console.log("Click", event);
});
