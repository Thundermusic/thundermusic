export { search } from "./youtube/search";
export { download } from "./youtube";

const audio = new Audio();

export async function addHandlers({
  onPositionChange,
  onPlay,
  onPause,
  onNext,
  onPrevious
}) {
  if (onPositionChange) {
    audio.addEventListener("timeupdate", () => {
      onPositionChange(audio.currentTime, audio.duration);
    });
  }

  if (onNext) {
    audio.addEventListener("ended", () => onNext());
  }

  if ("mediaSession" in navigator) {
    onPlay && navigator.mediaSession.setActionHandler("play", onPlay);
    onPause && navigator.mediaSession.setActionHandler("pause", onPause);
    onNext && navigator.mediaSession.setActionHandler("nexttrack", onNext);
    onPrevious &&
      navigator.mediaSession.setActionHandler("previoustrack", onPrevious);
    /*		navigator.mediaSession.setActionHandler("pause", () =>
		);
		navigator.mediaSession.setActionHandler("seekbackward", () =>
		);
		navigator.mediaSession.setActionHandler("seekforward", () =>
		); */
  }
}

export async function setMusic(music) {
  if ("mediaSession" in navigator) {
    navigator.mediaSession.metadata = new window.MediaMetadata({
      title: music.title,
      artist: music.channel,
      album: undefined,
      artwork: [{ src: music.thumbnail }]
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
