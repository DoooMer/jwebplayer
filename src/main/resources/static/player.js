// global event: space button will pause playback
document
    .addEventListener('keypress', e => {

        if (e.key !== " " || e.target.tagName === "INPUT") {
            return;
        }

        const player = document.getElementById('player');

        if (player.paused) {
            player.play();
        } else {
            player.pause();
        }

        e.stopImmediatePropagation();
        e.preventDefault();
    });

// slow fade playback and pause when volume is low
function fade(period, step, callback) {
    const timer = setInterval(() => {
        const player = document.getElementById('player');

        if (player.volume > step) {
            player.volume -= step;
        } else {
            player.volume = 0;
            callback();
            clearInterval(timer);
        }

        if (player.volume <= step) {
            player.pause();
        }
    }, period);
}

function playpause() {
    const player = document.getElementById('player');
    return player.paused ? player.play() : player.pause();
}

function volumeChange(volume) {
    const player = document.getElementById('player');
    player.volume = volume;
}

const WINDOW_TITLE = document.title;
const signals = new Signals();
const settings = new Settings();
const memesProvider = new MemesProvider();
const app = new Vue({
    el: '#app',
    // store,
    data: {
        // loading states
        loadingTrack: true,
        loadingPlaylist: true,
        // display settings form
        showSettings: false,
        // display track title above player
        showTitle: true,
        // display all tracks under player
        showPlaylist: true,
        // display random meme if not enabled playlist
        showMeme: false,
        memeUrl: null,
        // timer duration for each fade step
        muteInterval: 300,
        // saved previous volume state
        mute_prev: null,
        // flag is true when fade is activated
        isFade: false,
        // flag is true when repeat is enabled
        isRepeat: true,
        // current track data
        trackUrl: null,
        trackName: null,
        trackId: null,
        // playlist (all tracks)
        tracksTotal: 0,
        playlist: [],
        // do not disturb saved previous state
        dnd_prev: null,
        // search by playlist
        search: null,
        // playlist filtered by search (if set)
        tracks: [],
        // saved playlists
        playlists: [],
        playlistNew: null, // name for new playlist
        playlistNewModal: null // instance modal
    },
    created() {
        // load saved state
        this.showTitle = settings.getShowTitle();
        this.showPlaylist = settings.getShowPlaylist();
        this.showSettings = settings.getShowSettings();
        this.showMeme = settings.getShowMeme();
        this.isRepeat = settings.getRepeat();
        this.muteInterval = settings.getMuteInterval();

        // get link to current track on load app
        API.playbackCurrent()
            .then(response => {
                this.trackUrl = response.data.downloadUrl;
                this.trackName = response.data.name;
                this.trackId = response.data.id;
            })
            .catch(console.error)
            .finally(() => {
                this.loadingTrack = false;
            });

        // load playlist if enabled
        if (this.showPlaylist) {
            API.tracks()
                .then(response => {
                    this.tracksTotal = response.data.total;
                    this.tracks = this.playlist = response.data.tracks;
                })
                .catch(console.error)
                .finally(() => {
                    this.loadingPlaylist = false;
                });
        }

        // subscribe for changes in storage
        signals.listen(event => {
            switch (event.key) {
                case Signals.CTRL_TRACKID:
                    this.play(event.newValue);
                    break;
                case 'jwp_ctrl_tunnel':
                    this.handleCtrl(JSON.parse(event.newValue));
                    break;
            }
        });

        // subscribe for settings in storage
        settings.listen(event => {
            const value = JSON.parse(event.newValue);

            switch (event.key) {
                case Settings.SHOW_TITLE:
                    this.showTitle = value;
                    break;
                case Settings.SHOW_PLAYLIST:
                    this.showPlaylist = value;
                    break;
                case Settings.SHOW_SETTINGS:
                    this.showSettings = value;
                    break;
                case Settings.SHOW_MEME:
                    this.showMeme = value;
                    break;
                case Settings.REPEAT:
                    this.isRepeat = value;
                    break;
                case Settings.MUTE_INTERVAL:
                    this.muteInterval = value;
                    break;
            }
        })
    },
    mounted() {
        // init modal
        this.playlistNewModal = M.Modal.init(document.getElementById('playlist-new'));

        API.playlists()
            .then(response => {
                this.playlists = response.data.playlists;
            })
            .catch(console.error)
            .finally(() => {
                M.FormSelect.init(document.getElementById('playlist-selector'));
            });
    },
    watch: {
        showPlaylist: function (newValue) {
            settings.setShowPlaylist(newValue);
            // load playlist by toggle setting
            if (newValue) {
                this.showMeme = false;
                API.tracks()
                    .then(response => {
                        this.tracksTotal = response.data.total;
                        this.tracks = this.playlist = response.data.tracks;
                    })
                    .catch(console.error)
                    .finally(() => {
                        this.loadingPlaylist = false;
                    });
            }
        },
        showTitle: function (newValue) {
            settings.setShowTitle(newValue);
        },
        showSettings: function (newValue) {
            settings.setShowSettings(newValue);
        },
        showMeme: function (newValue) {
            settings.setShowMeme(newValue);

            if (newValue) {
                console.log('memes on, lets fun after 15 sec...');
                memesProvider.run((meme) => {
                    console.log('handle new meme');
                    let previewUrl = null;
                    let size = 0;
                    let reg = /width=(\d*)&/i;

                    for (let preview of meme.preview) {
                        let match = reg.exec(preview);
                        let currentSize = parseInt(match[1]);

                        if (size < currentSize) {
                            size = currentSize;
                        }

                        previewUrl = preview;

                        if (size >= 640) {
                            break;
                        }

                    }

                    this.memeUrl = previewUrl;

                }, 15_000);
            } else {
                console.log('... memes off');
                memesProvider.stop();
                this.memeUrl = null;
            }
        },
        isRepeat: function (newValue) {
            settings.setRepeat(newValue);
        },
        muteInterval: function (newValue) {
            settings.setMuteInterval(newValue);
        },
        search: function (newValue) {
            // filtering playlist by text (name)
            if (newValue == null || newValue.length < 1) {
                this.tracks = this.playlist;
                return;
            }

            const search = newValue.toLowerCase();

            this.tracks = this.playlist.filter(track => {
                return track.name.toLowerCase().includes(search);
            });
        },
        trackId: function (newValue) {
            localStorage.setItem('jwp_ctrl_trackId', newValue);
            document.title = this.trackName + ' | ' + WINDOW_TITLE;
        },
    },
    computed: {
        loading: function () {
            return this.loadingTrack && (this.showPlaylist && this.loadingPlaylist);
        },
    },
    methods: {
        next() {
            // get link to next track
            API.playbackNext()
                .then(response => {
                    this.trackUrl = response.data.downloadUrl;
                    this.trackName = response.data.name;
                    this.trackId = response.data.id;
                })
                .catch(e => {
                    console.error(e);
                    this.next();
                });
        },
        prev() {
            // todo: get link to previous track
        },
        fade() {
            // slow down volume and pause, or set volume to max
            const player = document.getElementById('player');

            // restore volume (max)
            if (player.volume < 0.1 || this.mute_prev !== null) {
                player.volume = this.mute_prev; // restore previous volume level
                this.mute_prev = null;
                return;
            }

            this.mute_prev = player.volume; // remember volume level
            this.isFade = true;

            fade(this.muteInterval, 0.1, () => {
                this.isFade = false
            });
        },
        play(id) {
            // get selected track and play
            API.playbackId(id)
                .then(response => {
                    this.trackUrl = response.data.downloadUrl;
                    this.trackName = response.data.name;
                    this.trackId = response.data.id;
                })
                .catch(console.error);
        },
        dnd() {
            // do not disturb
            if (!this.dnd_prev && (this.showTitle || this.showPlaylist)) {
                // save state
                this.dnd_prev = {
                    showTitle: this.showTitle,
                    showPlaylist: this.showPlaylist,
                };
                // on
                this.showTitle = false;
                this.showPlaylist = false;
                return;
            }

            if (this.showTitle ^ this.showPlaylist) {
                // save state
                this.dnd_prev = {
                    showTitle: this.showTitle,
                    showPlaylist: this.showPlaylist,
                };
                // on
                this.showTitle = false;
                this.showPlaylist = false;
                return;
            }

            // off, back to previous state
            if (this.dnd_prev) {
                this.showTitle = this.dnd_prev.showTitle;
                this.showPlaylist = this.dnd_prev.showPlaylist;
                this.dnd_prev = null;
            } else {
                this.showTitle = true;
                this.showPlaylist = true;
            }

        },
        meme() {
            if (!this.showPlaylist) {
                this.showMeme = !this.showMeme;
            }
        },
        handleCtrl(data) {
            console.debug(data);

            switch (data.action) {
                case CtrlEvent.NEXT:
                    this.next();
                    signals.reset();
                    break;
                case CtrlEvent.PLAYPAUSE:
                    let promise = playpause();

                    if (promise !== undefined) {
                        promise
                            .then(_ => {
                                console.log('Start playing remotely.');
                            })
                            .catch(() => {
                                console.log('Can\'t start playing remotely.');
                            });
                    }

                    signals.reset();
                    break;
                case CtrlEvent.VOLUMECHANGE:
                    volumeChange(data.value);
                    break;
                case CtrlEvent.MUTE:
                    this.fade();
                    signals.reset();
                    break;
                case CtrlEvent.DND:
                    this.dnd();
                    signals.reset();
                    break;
                case CtrlEvent.SYNCALL:
                    let player = document.getElementById('player');
                    signals.pushDataSyncAll({
                        volume: player.volume,
                        playpause: player.paused ? 'pause' : 'play',
                        mute: !!this.mute_prev,
                    });
                    break;
            }
        },
        syncPlayState(e) {
            signals.pushPlayState(e.type);
            switch (e.type) {
                case 'play':
                    document.title = this.trackName + ' | ' + WINDOW_TITLE;
                    break;
                case 'pause':
                    document.title = WINDOW_TITLE;
                    break;
            }
        },
        syncVolume(e) {
            signals.pushVolumeState(e.target.volume);
        },
        // create a new playlist
        playlistCreate() {
            API.createPlaylist(this.playlistNew)
                .then(() => {
                    this.playlistNew = null;
                    this.playlistNewModal.close();
                })
                .catch(console.error)
                .finally(() => {
                    // load update list
                    API.playlists()
                        .then(response => {
                            this.playlists = response.data.playlists;
                        })
                        .catch(console.error)
                        .finally(() => {
                            M.FormSelect.init(document.getElementById('playlist-selector'));
                        });
                });
        },
    }
});