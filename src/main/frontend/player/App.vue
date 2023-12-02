<script>
import Materialize from 'materialize-css';
import {Signals, Settings, API, CtrlEvent, playpause, volumeChange} from "../common/utils";
import PlayerTrackTitle from "./components/player/PlayerTrackTitle.vue";
import PlayerButtonPrev from "./components/player/PlayerButtonPrev.vue";
import PlayerButtonNext from "./components/player/PlayerButtonNext.vue";
import PlayerButtonRepeat from "./components/player/PlayerButtonRepeat.vue";
import PlaylistButtonNew from "./components/playlist/PlaylistButtonNew.vue";
import PlaylistSelector from "./components/playlist/PlaylistSelector.vue";
import PlayerButtonSettings from "./components/player/PlayerButtonSettings.vue";

const WINDOW_TITLE = document.title;
const signals = new Signals();
const settings = new Settings();

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

export default {
  components: {
    PlayerButtonSettings,
    PlaylistSelector, PlaylistButtonNew, PlayerButtonRepeat, PlayerButtonNext, PlayerButtonPrev, PlayerTrackTitle},
  data() {
    return {
      // loading states
      loadingTrack: true,
      loadingPlaylist: true,
      // display track title above player
      showTitle: true,
      // display all tracks under player
      showPlaylist: true,
      // // timer duration for each fade step
      // muteInterval: 300,
      // // saved previous volume state
      // mute_prev: null,
      // // flag is true when fade is activated
      // isFade: false,
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
      playlistSelectedToAddTrack: null, // which playlist selected
    };
  },
  created() {
    // load saved state
    this.showTitle = settings.getShowTitle();
    this.showPlaylist = settings.getShowPlaylist();
    this.isRepeat = settings.getRepeat();

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
      }
    })
  },
  mounted() {
    // init modal
    // this.playlistNewModal = Materialize.Modal.init(document.getElementById('playlist-new'));
    Materialize.Modal.init(document.querySelector('#playlist-add-track'));
    // Materialize.Modal.init(document.querySelector('#settings'));

    API.playlists()
        .then(response => {
          this.playlists = response.data.playlists;
        })
        .catch(console.error)
        .finally(() => {
          Materialize.FormSelect.init(document.getElementById('playlist-selector'));
          Materialize.FormSelect.init(document.getElementById('playlist-add-track-selector'));
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
    autoplayNext() {

      if (!this.isRepeat) {
        return;
      }

      this.$refs.nextBtn.next();
    },
    prev(event) {
      // todo: get link to previous track
    },
    repeat(event) {
      this.isRepeat = event;
    },
    toggleTitle(event) {
      console.log('showTitle: ', event);
      this.showTitle = event;
    },
    togglePlaylist(event) {
      this.showPlaylist = event;
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
    handleCtrl(data) {
      console.debug("Handle control:", data);

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
                .catch(e => {
                  console.log('Can\'t start playing remotely.');
                  console.debug(e);
                });
          }

          signals.reset();
          break;
        case CtrlEvent.VOLUMECHANGE:
          volumeChange(data.value);
          break;
        case CtrlEvent.SYNCALL:
          let player = document.getElementById('player');
          signals.pushDataSyncAll({
            volume: player.volume,
            playpause: player.paused ? 'pause' : 'play'
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
    playlistCreated(event) {
      console.log('Playlist created: ', event);
      Materialize.toast({html: 'Плейлист создан'});
      API.playlists()
          .then(response => {
            this.playlists = response.data.playlists;
          })
          .catch(e => {
            console.error('Ошибка обновления списка плейлистов: ', e);
            Materialize.toast({html: 'Список плейлистов не удалось обновить'});
          })
          .finally(() => {
            Materialize.FormSelect.init(document.getElementById('playlist-selector'));
            Materialize.FormSelect.init(document.getElementById('playlist-add-track-selector'));
          });
    },
    // add track to playlist
    addToPlaylist(trackId) {
      // console.log(trackId);
      this.playlistSelectedTrack = trackId;
      this.playlistSelectedToAddTrack = null;
      Materialize.FormSelect.init(document.getElementById('playlist-add-track-selector'));
    },
    addTrackToPlaylist() {

      if (this.playlistSelectedTrack === null || this.playlistSelectedToAddTrack === null) {
        console.log('track or playlist not selected');
        Materialize.toast({html: 'Ошибка добавления в плейлист: трек или плейлист не выбан'});
        return;
      }

      console.log('add track ' + this.playlistSelectedTrack + ' to playlist ' + this.playlistSelectedToAddTrack);
      API.addToPlaylist(this.playlistSelectedTrack, this.playlistSelectedToAddTrack)
          .then(() => {
            console.log('track added to playlist');
            const playlist = this.playlists.filter(playlist => playlist.id.includes(this.playlistSelectedToAddTrack)).pop();
            Materialize.toast({html: 'Трек добавлен в плейлист "' + playlist.name + '"'});
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
            Materialize.toast({html: 'Не удалось загрузить список треков для плейлиста'});
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
    //                     Materialize.toast({html: 'Не удалось загрузить список треков для плейлиста'});
    //                 });
    //         })
    //         .catch(e => {
    //             console.error(e);
    //             Materialize.toast({html: 'Ошибка переключения плейлиста'});
    //         });
    //
    // },
  }
}
</script>

<template>
  <div class="row" v-if="loading">
    <div class="col s4 offset-s4">
      <div class="row">
        <div class="col s12 center-align" style="margin-top: 50px;">
          <p style="text-transform: uppercase;">Загрузка...</p>
        </div>
      </div>
    </div>
  </div>
  <div class="row" v-show="!loading" style="display: none;">
    <div class="col s12">
      <PlayerTrackTitle :title="trackName" :visible="showTitle"></PlayerTrackTitle>
    </div>
    <div class="col s10">
      <audio autoplay controls :src="trackUrl" @ended="autoplayNext" @play="syncPlayState"
             @pause="syncPlayState" @volumechange="syncVolume" id="player"></audio>
    </div>
    <div class="col s2" style="padding: 11px 0;">
      <PlayerButtonPrev @prev="prev"></PlayerButtonPrev>
      <PlayerButtonNext ref="nextBtn" @next="next"></PlayerButtonNext>
      <PlayerButtonRepeat :repeat="isRepeat" @repeat="repeat"></PlayerButtonRepeat>
      <PlayerButtonSettings :title="showTitle" :playlist="showPlaylist" @toggle-title="toggleTitle"
                              @toggle-playlist="togglePlaylist"></PlayerButtonSettings>
    </div>
  </div>
  <div class="row" v-show="!loading" style="display: none;">
    <div class="col s9 offset-s3">
      <div class="input-field">
        <input type="text" placeholder="поиск" v-model="search" id="search"/>
        <label for="search"></label>
      </div>
    </div>
    <div class="col s3">
      <div style="margin-bottom: 25px;">
        <PlaylistButtonNew @playlist-created="playlistCreated"></PlaylistButtonNew>
      </div>
      <div v-for="unit in playlists">
        <PlaylistSelector :unit="unit" @playlist-selected="playlistSelected"></PlaylistSelector>
      </div>
    </div>
    <div class="col s9">
      <template v-if="showPlaylist">
        <p>Всего: {{ tracksTotal }}</p>
        <div class="frame">
          <ul v-if="tracks.length" class="scroll" id="playlist">
            <li v-for="track in tracks">
              <section>
                <div class="track-title" style="" @click.prevent="play(track.id)"
                     :title="track.directory + ' / ' + track.name"
                     :class="{'active': track.id === trackId}">
                  <template v-if="track.id === trackId">
                    <i class="material-icons tiny">play_arrow</i>&nbsp;
                  </template>
                  {{ track.name }}
                  <template v-if="track.directory">
                    <br><small class="grey-text">{{ track.directory }}</small>
                  </template>
                </div>
                <div class="track-actions" style="">
                                    <span class="playlist-button right modal-trigger" title="Добавить в плейлист"
                                          data-target="playlist-add-track" @click.prevent="addToPlaylist(track.id)">
                                        <i class="material-icons">add</i>
                                    </span>
                </div>
              </section>
            </li>
          </ul>
        </div>
        <div id="playlist-add-track" class="modal">
          <div class="modal-content">
            <h4>Выберите плейлист</h4>
            <div class="input-field">
              <select id="playlist-add-track-selector" v-model="playlistSelectedToAddTrack">
                <option v-for="unit in playlists" :value="unit.id">{{ unit.name }}</option>
              </select>
              <label for="playlist-add-track-selector">Плейлист</label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-close waves-effect waves-green btn" @click="addTrackToPlaylist">
              Добавить
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
</style>
