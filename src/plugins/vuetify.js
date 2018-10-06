import Vue from 'vue'
import {
  Vuetify,
  VApp,
  /*VNavigationDrawer,
  VFooter,
  VList,
  VBtn,
  VIcon,
  VGrid,
  VToolbar,*/
    VIcon,
    VBtn,
    VCard,
    VBottomNav,
  transitions
} from 'vuetify'
import 'vuetify/src/stylus/app.styl'
import fr from 'vuetify/es5/locale/fr'

Vue.use(Vuetify, {
  components: {
    VApp,
    /*VNavigationDrawer,
    VFooter,
    VList,
    VBtn,
    VIcon,
    VGrid,
    VToolbar,*/
      VBtn,
      VIcon,
      VCard,
      VBottomNav,

    transitions
  },
  theme: {
    primary: '#F67504',
    secondary: '#424242',
    accent: '#82B1FF',
    error: '#FF5252',
    info: '#2196F3',
    success: '#4CAF50',
    warning: '#FFC107'
  },
  customProperties: true,
  iconfont: 'md',
  lang: {
    locales: { fr },
    current: 'fr'
  },
})
