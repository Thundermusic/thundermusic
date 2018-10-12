const YOUTUBE_API_KEY = process.env.YOUTUBE_API_KEY

const API_URL = 'https://www.googleapis.com/youtube/v3'

function convertTime(duration) {
	const hours = duration.match(/(\d+)H/);
	const minutes = duration.match(/(\d+)M/);
	const seconds = duration.match(/(\d+)S/);
	
	const dur = [hours, minutes, seconds].map(e => e ? e[1] : '')
	if (!dur[0]) dur.splice(0, 1)
	
	return dur.map(e => e.padStart(2, '0')).join(':')
}

export async function search(query) {
	const results = await fetch(`${API_URL}/search?part=snippet&q=${encodeURIComponent(query)}&maxResults=50&type=video&key=${YOUTUBE_API_KEY}`)
	.then(res => res.json())
	return results.items.map(({ id: { videoId }, snippet: { title, channelTitle: channel, thumbnails: { default: { url: thumbnail }} }}) => ({
		id: videoId,
		title,
		channel,
		thumbnail,
		duration: null,
		youtube: {
			videoId
		},
	}));
}

export async function addDuration(searchResults) {
	const ids = searchResults.map(({ youtube: { videoId }}) => videoId)
	const { items } = await fetch(`${API_URL}/videos?part=contentDetails&id=${ids.join(',')}&key=${YOUTUBE_API_KEY}`)
	.then(res => res.json())
	items.forEach(({ contentDetails: { duration } }, i) => searchResults[i].duration = convertTime(duration))
}