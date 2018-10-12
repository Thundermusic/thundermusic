import { parse } from "querystring"
import FORMATS from "./formats"
import { getTokens, decipherFormat } from "./sig"

const CORS_PROXY = "https://cors-anywhere.herokuapp.com/"

const VIDEO_EURL = 'https://youtube.googleapis.com/v/';
const INFO_URL = 'https://www.youtube.com/get_video_info/';

function between(haystack, left, right)  {
	let pos = haystack.indexOf(left);
	if (pos === -1) { return ''; }
	haystack = haystack.slice(pos + left.length);
	pos = haystack.indexOf(right);
	if (pos === -1) { return ''; }
	haystack = haystack.slice(0, pos);
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
		formats = formats
		.concat(info.url_encoded_fmt_stream_map.split(','));
	}
	if (info.adaptive_fmts) {
		formats = formats.concat(info.adaptive_fmts.split(','));
	}
	
	formats = formats.map((format) => parse(format));
	delete info.url_encoded_fmt_stream_map;
	delete info.adaptive_fmts;
	
	return formats.map(addFormatMeta);
}

export async function getInfo(id) {
	console.log('ID', id)
	const body = await fetch(`${CORS_PROXY}https://www.youtube.com/embed/${id}?hl=en`)
	.then(res => res.text())
	const config = JSON.parse(between(body, 't.setConfig({\'PLAYER_CONFIG\': ', '},\'') + '}')
	const html5player = config.assets.js

	// Fetch early
	const tokens = getTokens(`${CORS_PROXY}https://www.youtube.com${html5player}`)
	
	const infos = parse(await fetch(`${CORS_PROXY}${INFO_URL}?video_id=${id}&eurl=${VIDEO_EURL + id}&ps=default&gl=US&hl=en&sts=${config.sts}`)
	.then(res => res.text()))
	
	const formats = parseFormats(infos)

	const audioOnly = formats.filter(({ bitrate, audioBitrate }) => !bitrate && audioBitrate)

	const downloadable = audioOnly.filter(({ live, isHLS, isDashMPD }) => !live && !isHLS && !isDashMPD)
		.sort(({ audioBitrate: aAudioBitrate }, { audioBitrate: bAudioBitrate }) => bAudioBitrate - aAudioBitrate)

	console.log(downloadable)

	const [toDownload] = downloadable

	decipherFormat(toDownload, await tokens)

	console.log(infos, config)

	return toDownload.url

	/*await fetch(downloadable[0].url, {
		mode: 'no-cors'
	})*/
}