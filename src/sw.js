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

self.addEventListener('message', (event) => {
	switch(event.data.type) {
		case 'downloadTo':
			event.waitUntil((async () => {
				const { to, from } = event.data
				const res = await fetch(from)
				const cache = await caches.open("musics")
				DOWNLOADING.set(to, res)
				event.ports[0].postMessage(to)
				await cache.put(to, res.clone())
				DOWNLOADING.delete(to)
			})())
			break;
		case 'hasDownloaded':
			event.waitUntil((async () => {
				const { url } = event.data
				const cache = await caches.open("musics")
				event.ports[0].postMessage(!! (await cache.match(url)))
			})())
			break;
	}
});

workbox.routing.registerRoute(
	new RegExp('/musics/'),
	async ({event}) => {
		const url = event.request.url;
		const urlEnd = url.slice(url.indexOf('/musics/'))
		
		if (DOWNLOADING.has(urlEnd)) {
			return DOWNLOADING.get(urlEnd);
		}
		const cache = await caches.open("musics")
		const res = await cache.match(event.request)

		return res;
	}
);

self.addEventListener('backgroundfetchsuccess', (event) => {
	console.log('SUCCESS', event)
});

self.addEventListener('backgroundfetchfail', (event) => {
	console.log('FAIL', event)
});

self.addEventListener('backgroundfetchabort', (event) => {
	console.log('ABORT', event)
});

self.addEventListener('backgroundfetchclick', (event) => {
	console.log('Click', event)
});