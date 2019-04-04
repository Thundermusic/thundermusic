export { getPlaylistContent, getPlaylistInfos } from "../web/youtube/api";
export { default as providers } from "../web/providers";
import * as storage from "./storage";
export { storage };
export { cleanupMusic } from "./sw-client";
import { callbacks } from "./listener";

import ImportFromYoutube from "../../components/create-playlist/ImportFromYoutube";

export const playlistComponents = [ImportFromYoutube];
export const routerMode = "hash";

export async function addHandlers(handlers) {
  callbacks.media = { ...callbacks.media, ...handlers };
}

export async function setMusic(music) {
  // eslint-disable-next-line no-undef
  cordova.exec(() => {}, () => {}, "Thundermusic", "play", [music]);
}
export async function play() {
  // eslint-disable-next-line no-undef
  cordova.exec(() => {}, () => {}, "Thundermusic", "unpause");
}

export async function pause() {
  // eslint-disable-next-line no-undef
  cordova.exec(() => {}, () => {}, "Thundermusic", "pause");
}

export function seek(position) {
  // eslint-disable-next-line no-undef
  cordova.exec(() => {}, () => {}, "Thundermusic", "seek", [position * 1000]);
}

export function setVolume() {}
