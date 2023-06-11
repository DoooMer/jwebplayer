<script>
import {Signals, Settings, API, CtrlEvent} from "../common/utils";

const signals = new Signals();
const settings = new Settings();

export default {
  data() {
    return {
      loading: true,
      showTitle: true,
      showPlaylist: false,
      isRepeat: true,
      trackId: null,
      // playlist (all tracks)
      tracksTotal: 0,
      playlist: [],
      // search by playlist
      search: null,
      // playlist filtered by search (if set)
      tracks: [],
      playState: undefined,
      volume: 1,
    };
  },
  created() {
    // load current state
    this.showTitle = settings.getShowTitle();
    this.showPlaylist = settings.getShowPlaylist();
    this.isRepeat = settings.getRepeat();
    this.trackId = signals.getTrackId();

    // sync not stored state of player
    signals.pushRequestSyncAll();

    // load tracks list
    API.tracks()
        .then(response => {
          this.tracksTotal = response.data.total;
          this.tracks = this.playlist = response.data.tracks;
        })
        .catch(console.error);

    // subscribe for events in storage
    signals.listen(event => {
      switch (event.key) {
        case Signals.CTRL_TRACKID:
          this.trackId = event.newValue;
          break;
        case Signals.CTRL_TUNNEL:
          this.handleCtrl(JSON.parse(event.newValue));
          break;
      }
    });

    // subscribe for settings in storage
    settings.listen(event => {
      let value = JSON.parse(event.newValue);

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
    });

    this.loading = false;
  },
  watch: {
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
      signals.pushTrackId(newValue);
    },
    showTitle: function (newValue) {
      settings.setShowTitle(newValue);
    },
    showPlaylist: function (newValue) {
      settings.setShowPlaylist(newValue);
      this.showMeme = newValue ? false : this.showMeme;
    },
    isRepeat: function (newValue) {
      settings.setRepeat(newValue);
    },
    volume: function (newValue) {
      signals.pushVolumeChange(newValue);
    },
  },
  methods: {
    play(id) {
      this.trackId = id;
    },
    handleCtrl(data) {
      switch (data.action) {
        case CtrlEvent.PLAYSTATE:
          this.playState = data.value;
          break;
        case CtrlEvent.VOLUMESTATE:
          this.volume = data.value;
          break;
        case CtrlEvent.DATASYNCALL:
          this.volume = data.value.volume;
          this.mute = data.value.mute;
          this.playState = data.value.playpause;
          break;
      }
    },
    prev() {

    },
    next() {
      signals.pushNext();
    },
    playpause() {
      signals.pushPlayPause();
    },
  },
}
</script>

<template>
  <div class="row" v-if="loading">
    Загрузка...
  </div>
  <div class="row" v-show="!loading" style="display: none;">
    <div class="col s3">
      <p>
        <label>
          <input type="checkbox" v-model="showTitle"/>
          <span>Показать название</span>
        </label>
      </p>
      <p>
        <label>
          <input type="checkbox" v-model="showPlaylist"/>
          <span>Показать плейлист</span>
        </label>
      </p>
      <p>
        <label>
          <input type="checkbox" v-model="isRepeat"/>
          <span>Повтор</span>
        </label>
      </p>
      <p>Всего треков: {{ tracksTotal }}</p>
      <div>
        <p>Громкость ({{ volume.toFixed(4) }})</p>
        <input type="range" v-model="volume" min="0.0001" max="1" step="0.001"/>
      </div>
      <div>
        <button @click.prevent="" class="btn-flat waves-effect waves-teal btn-large">
          <i class="material-icons">skip_previous</i>
        </button>
        <button @click.prevent="playpause" class="waves-effect waves-teal btn-large"
                :class="{'btn-flat': playState !== 'play', 'btn': playState === 'play'}"
                :disabled="playState === undefined">
          <i class="material-icons">play_arrow</i>
        </button>
        <button @click.prevent="next" class="btn-flat waves-effect waves-teal btn-large">
          <i class="material-icons">skip_next</i>
        </button>
      </div>
    </div>
    <div class="col s9">
      <div class="input-field">
        <input type="text" placeholder="поиск" v-model="search" id="search"/>
        <label for="search"></label>
      </div>
      <div class="frame">
        <ul v-if="tracks.length" class="scroll">
          <li v-for="track in tracks">
            <section>
              <a href="#" @click.prevent="play(track.id)" :title="track.name + ' | ' + track.id">
                <template v-if="track.id === trackId">
                  <i class="material-icons tiny">play_arrow</i>&nbsp;
                </template>
                {{ track.name }}
                <template v-if="track.directory">
                  <br>
                  <small class="grey-text">{{ track.directory }}</small>
                </template>
              </a>
            </section>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.frame .scroll li {
  padding-top: 6px;
  padding-bottom: 6px;
}
</style>
