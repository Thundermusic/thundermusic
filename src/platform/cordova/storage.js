/* eslint-disable no-undef */

import { musics, callbacks, init } from "./listener";

export async function addMusic(music) {
  return new Promise((resolve, reject) => {
    cordova.exec(resolve, reject, "Thundermusic", "download", [music]);
  });
}

export async function deleteMusic(id) {
  return new Promise((resolve, reject) => {
    cordova.exec(resolve, reject, "Thundermusic", "remove", [id]);
  });
}

export function updateMusic(music) {
  return new Promise((resolve, reject) => {
    cordova.exec(resolve, reject, "Thundermusic", "update", [music]);
  });
}

export function getMusics() {
  return new Promise(resolve => {
    if (musics === null) {
      init();

      callbacks.musics = () => {
        resolve(musics);
      };
    } else {
      resolve(musics);
    }
  });
}

// eslint-disable-next-line no-unused-vars
export async function addPlaylist(playlist) {
  // TODO: Playlists
}

export async function getPlaylists() {
  // TODO: Playlists
  return [];
}

let settings = null;

export async function setSetting(key, value) {
  if (settings == null) {
    await getSettings();
  }

  settings[key] = value;
  localStorage.setItem("settings", JSON.stringify(settings));
}

export async function getSettings() {
  if (settings == null) {
    settings = JSON.parse(localStorage.getItem("settings") || "{}");
  }

  const result = [];
  for (const key of Object.keys(settings)) {
    result.push([key, settings[key]]);
  }

  return result;
}

/*
    channel
    duration
    id
    thumbnail
    title
    url
    youtube => videoID
 */
