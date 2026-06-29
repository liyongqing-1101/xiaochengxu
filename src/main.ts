import { createSSRApp } from 'vue'
import App from './App.vue'
import pinia from './stores'

import './styles/reset.scss'
import './styles/global.scss'
import './styles/vant-override.scss'

export function createApp() {
  const app = createSSRApp(App)
  app.use(pinia)
  return {
    app,
    pinia,
  }
}
