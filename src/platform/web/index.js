export { getPlaylistContent, getPlaylistInfos } from "./youtube/api";
export { default as providers } from "./providers";
import * as storage from "platform/storage";
import * as notification from "platform/notification";
export { storage };
export { cleanupMusic } from "platform/sw-client";

import ImportFromYoutube from "../../components/create-playlist/ImportFromYoutube";

export const playlistComponents = [ImportFromYoutube];

const audio = new Audio();
audio.volume = (localStorage.getItem("volume") || 100) / 100;

export async function addHandlers({
  onPositionChange,
  onPlay,
  onPause,
  onNext,
  onEnd,
  onPrevious
}) {
  if (onPositionChange) {
    audio.addEventListener("timeupdate", () => {
      onPositionChange(audio.currentTime, audio.duration);
    });
  }

  if (onEnd) {
    audio.addEventListener("ended", () => onEnd());
  }

  notification.addHandlers({ onPlay, onPause, onNext, onPrevious });
}

export async function setMusic(music) {
  notification.setMusic(music);
  audio.src = await music.url;
}
export function play() {
  return audio.play();
}

export function pause() {
  return audio.pause();
}

export function seek(position) {
  audio.currentTime = position;
}

export function setVolume(volume) {
  audio.volume = volume / 100;
}
