import Vue from "vue";
import {
  Vuetify,
  VApp,
  VGrid,
  VList,
  VDivider,
  VIcon,
  VBtn,
  VCard,
  VBottomNav,
  VAvatar,
  VTextField,
  VToolbar,
  VForm,
  VProgressLinear,
  VProgressCircular,
  VSlider,
  VNavigationDrawer,
  VSubheader,
  VDialog,
  VExpansionPanel,
  VMenu,
  VSwitch,
  transitions
} from "vuetify";

import "vuetify/src/stylus/app.styl";
import fr from "vuetify/es5/locale/fr";

Vue.use(Vuetify, {
  components: {
    VApp,
    VGrid,
    VList,
    VDivider,
    VBtn,
    VIcon,
    VCard,
    VBottomNav,
    VAvatar,
    VTextField,
    VToolbar,
    VForm,
    VProgressLinear,
    VProgressCircular,
    VSlider,
    VNavigationDrawer,
    VSubheader,
    VDialog,
    VExpansionPanel,
    VMenu,
    VSwitch,

    transitions
  },
  theme: {
    primary: "#F67504",
    secondary: "#424242",
    accent: "#F67504",
    error: "#FF5252",
    info: "#2196F3",
    success: "#4CAF50",
    warning: "#FFC107"
  },
  customProperties: true,
  iconfont: "md",
  lang: {
    locales: { fr },
    current: "fr"
  }
});
