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

function scrollPlaylistIntoActiveItem() {
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

Vue.component('track-title', {
    props: ['title', 'visible'],
    template: `
        <h5 v-if="visible && title">{{ title }}</h5>
        <h5 v-else>&nbsp;</h5>`,
});
Vue.component('player-button-prev', {
    template: `
        <button @click.prevent="prev" class="btn-flat waves-effect waves-teal" title="Предыдущий трек">
            <i class="material-icons">skip_previous</i>
        </button>`,
    methods: {
        prev() {

        },
    },
});
Vue.component('player-button-next', {
    template: `
        <button @click.prevent="next" class="btn-flat waves-effect waves-teal" title="Следующий трек">
            <i class="material-icons">skip_next</i>
        </button>`,
    methods: {
        next() {
            // get link to next track
            API.playbackNext()
                .then(response => {
                    this.$emit('next', {
                        trackUrl: response.data.downloadUrl,
                        trackName: response.data.name,
                        trackId: response.data.id,
                    })
                })
                .catch(e => {
                    console.error(e);
                    this.next();
                });
        },
    },
});
Vue.component('player-button-repeat', {
    props: ['repeat'],
    template: `
        <button @click.prevent="toggle" :class="{'teal white-text': repeat}" class="btn-flat"
                title="Режим повтора">
            <i class="material-icons">repeat</i>
        </button>`,
    methods: {
        toggle() {
            this.$emit('repeat', !this.repeat);
        },
    },
});
Vue.component('player-button-settings', {
    props: ['title', 'playlist'],
    data() {
        return {
            showTitle: this.title,
            showPlaylist: this.playlist,
        };
    },
    watch: {
        showTitle() {
            this.$emit('toggleTitle', this.showTitle);
        },
        showPlaylist() {
            this.$emit('togglePlaylist', this.showPlaylist);
        },
    },
    template: `
        <div style="display: inline-block;">
            <a class="waves-effect waves-light btn-flat modal-trigger" title="Настройки" href="#settings">
                <i class="material-icons">settings</i>
            </a>
            <div id="settings" class="modal bottom-sheet">
                <div class="modal-content">
                    <h4>Настройки</h4>
                    <div class="row">
                        <div class="col s2">
                            <p>
                                <label>
                                    <input type="checkbox" v-model="showTitle"/>
                                    <span>Показать название</span>
                                </label>
                            </p>
                        </div>
                        <div class="col s2">
                            <p>
                                <label>
                                    <input type="checkbox" v-model="showPlaylist"/>
                                    <span>Показать плейлист</span>
                                </label>
                            </p>
                        </div>
<!--                        <div class="col s2">-->
<!--                            <div class="input-field">-->
<!--                                <input type="number" min="200" max="1500" step="100" v-model="muteInterval"-->
<!--                                       id="muteInterval"/>-->
<!--                                <label for="muteInterval">Скорость выключения звука</label>-->
<!--                                <span class="helper-text">мс</span>-->
<!--                            </div>-->
<!--                        </div>-->
                    </div>
<!--                    <div class="row">-->
<!--                        <div class="col s2">-->
<!--                            <p>-->
<!--                                <a th:href="@{/controls}" target="_blank">Управление</a>-->
<!--                            </p>-->
<!--                        </div>-->
<!--                    </div>-->
                </div>
            </div>
        </div>`,
    mounted() {
        M.Modal.init(document.querySelector('#settings'));
    },
});
Vue.component('new-playlist-button', {
    data() {
        return {
            playlistName: '',
            modal: null,
        };
    },
    template: `
        <div class="inline">
            <button data-target="playlist-new" class="btn modal-trigger btn-block">Новый плейлист</button>
            <div id="playlist-new" class="modal">
                <div class="modal-content">
                    <h4>Новый плейлист</h4>
                    <label>
                        <input type="text" v-model="playlistName" placeholder="Название"/>
                    </label>
                </div>
                <div class="modal-footer">
                    <button class="modal-close waves-effect waves-green btn" @click="playlistCreate">Добавить
                    </button>
                </div>
            </div>
        </div>`,
    mounted() {
        this.modal = M.Modal.init(document.getElementById('playlist-new'));
    },
    methods: {
        playlistCreate() {
            API.createPlaylist(this.playlistName)
                .then(() => {
                    this.playlistName = null;
                    this.modal.close();
                    this.$emit('playlistCreated');
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
                            M.FormSelect.init(document.getElementById('playlist-add-track-selector'));
                        });
                });
        },
    },
});
Vue.component('playlist-selector', {
    props: ['unit'],
    template: `
        <button class="btn btn-block btn-flat waves-effect waves-teal hover-teal truncate" :title="unit.name"
                @click.prevent="selectPlaylist(unit.id)">
            {{ unit.name }}
        </button>`,
    methods: {
        selectPlaylist(id) {
            API.selectPlaylist(id)
                .then(() => {
                    this.$emit('playlistSelected', id);
                })
                .catch(e => {
                    console.error('Ошибка выбора плейлиста: ', e);
                    M.toast({html: 'Ошибка переключения плейлиста'});
                });

        },
    },
});

const WINDOW_TITLE = document.title;
const signals = new Signals();
const settings = new Settings();
const app = new Vue({
    el: '#app',
    // store,
    data: {
        // loading states
        loadingTrack: true,
        loadingPlaylist: true,
        // display track title above player
        showTitle: true,
        // display all tracks under player
        showPlaylist: true,
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
        playlistNewModal: null, // instance modal
        // adding track to playlist
        playlistSelectedTrack: null, // which track selected
        playlistSelectedToAddTrack: null // which playlist selected
    },
    created() {
        // load saved state
        this.showTitle = settings.getShowTitle();
        this.showPlaylist = settings.getShowPlaylist();
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

        // loading tracks

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
        // this.playlistNewModal = M.Modal.init(document.getElementById('playlist-new'));
        M.Modal.init(document.querySelector('#playlist-add-track'));
        // M.Modal.init(document.querySelector('#settings'));

        API.playlists()
            .then(response => {
                this.playlists = response.data.playlists;
            })
            .catch(console.error)
            .finally(() => {
                M.FormSelect.init(document.getElementById('playlist-selector'));
                M.FormSelect.init(document.getElementById('playlist-add-track-selector'));
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
                    scrollPlaylistIntoActiveItem();
                });
        }
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
        next(event) {
            this.trackUrl = event.trackUrl;
            this.trackName = event.trackName;
            this.trackId = event.trackId;
            scrollPlaylistIntoActiveItem();
        },
        prev(event) {
            // todo: get link to previous track
        },
        repeat(event) {
            this.isRepeat = event;
        },
        toggleTitle(event) {
            this.showTitle = event;
        },
        togglePlaylist(event) {
            this.showPlaylist = event;
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
        // playlistCreate() {
        //     API.createPlaylist(this.playlistNew)
        //         .then(() => {
        //             this.playlistNew = null;
        //             this.playlistNewModal.close();
        //             M.toast({html: 'Плейлист создан'});
        //         })
        //         .catch(console.error)
        //         .finally(() => {
        //             // load update list
        //             API.playlists()
        //                 .then(response => {
        //                     this.playlists = response.data.playlists;
        //                 })
        //                 .catch(console.error)
        //                 .finally(() => {
        //                     M.FormSelect.init(document.getElementById('playlist-selector'));
        //                     M.FormSelect.init(document.getElementById('playlist-add-track-selector'));
        //                 });
        //         });
        // },
        playlistCreated() {
            M.toast({html: 'Плейлист создан'});
            API.playlists()
                .then(response => {
                    this.playlists = response.data.playlists;
                })
                .catch(e => {
                    console.error('Ошибка обновления списка плейлистов: ', e);
                    M.toast({html: 'Список плейлистов не удалось обновить'});
                })
                .finally(() => {
                    M.FormSelect.init(document.getElementById('playlist-selector'));
                    M.FormSelect.init(document.getElementById('playlist-add-track-selector'));
                });
        },
        // add track to playlist
        addToPlaylist(trackId) {
            // console.log(trackId);
            this.playlistSelectedTrack = trackId;
            this.playlistSelectedToAddTrack = null;
            M.FormSelect.init(document.getElementById('playlist-add-track-selector'));
        },
        addTrackToPlaylist() {

            if (this.playlistSelectedTrack === null || this.playlistSelectedToAddTrack === null) {
                console.log('track or playlist not selected');
                M.toast({html: 'Ошибка добавления в плейлист: трек или плейлист не выбан'});
                return;
            }

            console.log('add track ' + this.playlistSelectedTrack + ' to playlist ' + this.playlistSelectedToAddTrack);
            API.addToPlaylist(this.playlistSelectedTrack, this.playlistSelectedToAddTrack)
                .then(() => {
                    console.log('track added to playlist');
                    const playlist = this.playlists.filter(playlist => playlist.id.includes(this.playlistSelectedToAddTrack)).pop();
                    M.toast({html: 'Трек добавлен в плейлист "' + playlist.name + '"'});
                })
                .catch(console.error)
                .finally(() => {
                    this.playlistSelectedTrack = null;
                    this.playlistSelectedToAddTrack = null;
                });
        },
        playlistSelected(playlistId) {
            API.playlist(playlistId)
                .then(response => {
                    this.tracksTotal = response.data.total;
                    this.tracks = this.playlist = response.data.playlistTracks || [];
                })
                .catch(e => {
                    console.error('Ошибка загрузки списка треков из плейлиста: ', e);
                    M.toast({html: 'Не удалось загрузить список треков для плейлиста'});
                });
        },
        // selectPlaylist(playlistId) {
        //     API.selectPlaylist(playlistId)
        //         .then(() => {
        //             API.playlist(playlistId)
        //                 .then(response => {
        //                     this.tracksTotal = response.data.total;
        //                     this.tracks = this.playlist = response.data.playlistTracks || [];
        //                 })
        //                 .catch(e => {
        //                     console.error(e);
        //                     M.toast({html: 'Не удалось загрузить список треков для плейлиста'});
        //                 });
        //         })
        //         .catch(e => {
        //             console.error(e);
        //             M.toast({html: 'Ошибка переключения плейлиста'});
        //         });
        //
        // },
    }
});