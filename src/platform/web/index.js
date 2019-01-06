export { getPlaylistContent, getPlaylistInfos } from "./youtube/api";
export { default as providers } from "./providers";
import * as storage from "platform/storage";
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

  if ("mediaSession" in navigator) {
    onPlay && navigator.mediaSession.setActionHandler("play", onPlay);
    onPause && navigator.mediaSession.setActionHandler("pause", onPause);
    onNext && navigator.mediaSession.setActionHandler("nexttrack", onNext);
    onPrevious &&
      navigator.mediaSession.setActionHandler("previoustrack", onPrevious);
    /*
		navigator.mediaSession.setActionHandler("seekbackward", () => {});
		navigator.mediaSession.setActionHandler("seekforward", () => {});
    */
  }
}

export async function setMusic(music) {
  if ("mediaSession" in navigator) {
    navigator.mediaSession.metadata = new window.MediaMetadata({
      title: music.title,
      artist: music.channel,
      album: undefined,
      artwork: [
        {
          src: music.thumbnail || require("../../assets/thumbnail_default.png")
        }
      ]
    });
  }
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
