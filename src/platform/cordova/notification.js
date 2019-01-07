/* eslint-disable no-undef */

import { callbacks } from "./listener";

export async function addHandlers(handlers) {
  callbacks.media = handlers;
}

export function setMusic(music) {
  cordova.exec(() => {}, () => {}, "Thundermusic", "set-infos", [music]);
}
