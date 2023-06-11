import { createApp } from 'vue'
// import './style.css'
import App from './App.vue'
import { playpause } from "../common/utils";

const app = createApp(App);

// global event: space button will pause playback
document
    .addEventListener('keypress', e => {

        if (e.key !== " " || e.target.tagName === "INPUT") {
            return;
        }

        playpause();

        e.stopImmediatePropagation();
        e.preventDefault();
    });

app.mount('#app');