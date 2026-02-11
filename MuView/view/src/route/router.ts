import Overview from "@com/Overview.vue";
import ServerManager from "@com/Server/ServerManager.vue";
import EnvironmentManager from "@com/EnvironmentManager.vue";
import AboutPage from "@com/AboutPage.vue";
import Settings from "@com/Settings.vue";
import {createRouter, createWebHistory} from "vue-router";
import ServerPage from "@com/Server/ServerPage.vue";

const routes = [
    { path: '/', component: Overview },
    { path: '/servermanager', component: ServerManager },
    { path: '/server/:name', component: ServerPage, props: true },
    { path: '/envmanager', component: EnvironmentManager },
    { path: '/about', component: AboutPage },
    { path: '/settings', component: Settings },
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})