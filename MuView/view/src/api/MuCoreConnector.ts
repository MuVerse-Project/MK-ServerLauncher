import axios from "axios";

const backend = document.URL.split(new RegExp("/+"))[1]

export const apiClient = axios.create({
    baseURL: `${document.URL.split(new RegExp("/+"))[0]}//${backend.split(":")[0]}:20038`, //TODO: Product版更改
    allowAbsoluteUrls: true,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    }
});

export class MuWebSocket {
    private backend = `ws://${backend.split(":")[0]}`  // TODO: 多前端
    private port = 20038 /*document.URL.split(new RegExp("/+"))[1].split(":")[1]*/ //TODO: Product版更改
    private readonly instance: WebSocket;
    private msg: any
    private isConnected = false

    constructor(path: String) {
        this.instance = new WebSocket(`${this.backend}:${this.port}/${path}`)
        this.instance.onopen = (e) => {
            console.log("WebSocket Connected >> " + e)
            this.isConnected = true
        }
        this.instance.onerror = (e) => {
            console.log("Websocket Occurred an Error! >> " + e.type.toString())
            this.instance.close()
        }
        this.instance.onmessage = (e) => {
            this.msg = e.data
        }
        this.instance.onclose = (e) => {
            console.log("Websocket Closed! >> " + e.reason.toString())
            this.isConnected = false
        }
    }

    public getMsg(): any {
        if(this.isConnected){
            return JSON.parse(this.msg)
        }else{
            return undefined
        }
    }

    public isConnect(): boolean{
        return this.isConnected
    }

    public send(jsonMsg: any): any {
        if(this.instance && this.instance.readyState === WebSocket.OPEN){
            this.instance.send(JSON.stringify(jsonMsg))
            return this.getMsg()
        }else{
            console.warn("Error occurred while send MSG to MuCore, probably MuCore OFFLINE")
        }
    }

    public close(){
        if(this.isConnected){
            this.isConnected = false
            this.instance.close()
        }
    }
}