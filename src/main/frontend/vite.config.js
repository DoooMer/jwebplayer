import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            }
        }
    },
    build: {
        manifest: false,
        outDir: '../resources/static/player',
        emptyOutDir: true,
        rollupOptions: {
            input: {
                'player': 'player/main.js',
                'controls': 'controls/main.js'
            },
            output: {
                entryFileNames: '[name].js'
            }
        }
    }
})
