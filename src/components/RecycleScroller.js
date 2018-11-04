import { RecycleScroller } from "vue-virtual-scroller";
import "vue-virtual-scroller/dist/vue-virtual-scroller.css";

export default {
  mixins: [RecycleScroller],
  methods: {
    getScroll() {
      const top = window.scrollY;
      return {
        top,
        bottom: top + window.innerHeight
      };
    }
  },
  mounted() {
    window.addEventListener("scroll", this.handleScroll);
  }
};
