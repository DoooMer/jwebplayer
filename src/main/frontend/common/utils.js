import axios from "axios";

export function playpause() {
    const player = document.getElementById('player');
    return player.paused ? player.play() : player.pause();
}

export function volumeChange(volume) {
    const player = document.getElementById('player');
    player.volume = volume;
}

export function scrollPlaylistIntoActiveItem() {
    const playlistContainer = document.querySelector('#playlist');

    if (!playlistContainer) {
        return;
    }

    const active = playlistContainer.querySelector('.active');

    if (!active) {
        return;
    }

    active.scrollIntoView();
}

export class Signals {

    static CTRL_TRACKID = 'jwp_ctrl_trackId';
    static CTRL_TUNNEL = 'jwp_ctrl_tunnel';

    pushNext() {
        this._push({
            action: CtrlEvent.NEXT,
        });
    }

    pushPlayState(state) {
        this._push({
            action: CtrlEvent.PLAYSTATE,
            value: state,
        });
    }

    pushVolumeState(volume) {
        this._push({
            action: CtrlEvent.VOLUMESTATE,
            value: volume,
        });
    }

    pushPlayPause() {
        this._push({
            action: CtrlEvent.PLAYPAUSE,
        });
    }

    pushVolumeChange(value) {
        this._push({
            action: CtrlEvent.VOLUMECHANGE,
            value: value,
        });
    }

    pushRequestSyncAll() {
        this._push({
            action: CtrlEvent.SYNCALL,
        });
    }

    pushDataSyncAll(data) {
        this._push({
            action: CtrlEvent.DATASYNCALL,
            value: data,
        });
    }

    pushTrackId(id) {
        localStorage.setItem(Signals.CTRL_TRACKID, id);
    }

    getTrackId() {
        return localStorage.getItem(Signals.CTRL_TRACKID) || null;
    }

    _push(event) {
        localStorage.setItem(Signals.CTRL_TUNNEL, JSON.stringify(event));
    }

    reset() {
        localStorage.setItem(Signals.CTRL_TUNNEL, null);
    }

    listen(callback) {
        window.addEventListener('storage', callback);
    }
}

export class Settings {
    static SHOW_TITLE = 'jwp_showTitle';
    static SHOW_PLAYLIST = 'jwp_showPlaylist';
    static SHOW_SETTINGS = 'jwp_showSettings';
    static REPEAT = 'jwp_repeat';

    getShowTitle() {
        return JSON.parse(localStorage.getItem(Settings.SHOW_TITLE) || 'true');
    }

    setShowTitle(value) {
        localStorage.setItem(Settings.SHOW_TITLE, JSON.stringify(value));
    }

    getShowPlaylist() {
        return JSON.parse(localStorage.getItem(Settings.SHOW_PLAYLIST) || 'false');
    }

    setShowPlaylist(value) {
        localStorage.setItem(Settings.SHOW_PLAYLIST, JSON.stringify(value));
    }

    getRepeat() {
        return JSON.parse(localStorage.getItem(Settings.REPEAT) || 'true');
    }

    setRepeat(value) {
        localStorage.setItem(Settings.REPEAT, JSON.stringify(value));
    }

    listen(callback) {
        window.addEventListener('storage', event => {

            if (Settings.events().includes(event.key)) {
                callback(event);
            }

        });
    }

    static events() {
        return [
            Settings.SHOW_TITLE,
            Settings.SHOW_PLAYLIST,
            Settings.SHOW_SETTINGS,
            Settings.REPEAT,
        ];
    }
}

export class API {

    static tracks() {
        return axios.get('/api/tracks');
    }

    static playbackCurrent() {
        return axios.get('/api/playback/current');
    }

    static playbackNext() {
        return axios.get('/api/playback/next');
    }

    static playbackId(id) {
        return axios.get('/api/playback/' + id);
    }

    static playlists() {
        return axios.get('/api/playlists');
    }

    static createPlaylist(name) {
        return axios.post('/api/playlists', {"name": name});
    }

    static playlist(id) {
        return axios.get('/api/playlists/' + id + '/tracks');
    }

    static selectPlaylist(id) {
        return axios.post('/api/playlists/' + id + '/select');
    }

    static addToPlaylist(trackId, playlistId) {
        return axios.post('/api/playlists/' + playlistId, {"trackId": trackId});
    }

}

export class CtrlEvent {

    static NEXT = 'next';
    static PLAYPAUSE = 'playpause';
    static VOLUMECHANGE = 'volumeChange';
    static SYNCALL = 'syncAll';
    static PLAYSTATE = 'playState';
    static VOLUMESTATE = 'volumeState';
    static DATASYNCALL = 'dataSyncAll';

}