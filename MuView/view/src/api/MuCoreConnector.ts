import axios, {type AxiosInstance, type AxiosRequestConfig, type AxiosResponse} from "axios";
import "@/custom/api/MuMsgPacket.ts";
import {MuPacket, type MuPacketData} from "@/api/mupacket/MuPacket.ts";
import {MuPacketRegistry} from "@/api/mupacket/MuPacketFactory.ts";

const resolveWebSocketUrl = (api: string) => {
    if (/^wss?:\/\//i.test(api)) {
        return api
    }

    const protocol = window.location.protocol === "https:" ? "wss" : "ws"
    const path = api.startsWith("/") ? api : `/${api}`
    // return `${protocol}://${window.location.host}${path}`
    return `ws://127.0.0.1:20038${path}`
}

interface MuPacketAxiosConfig extends AxiosRequestConfig{
    usingMuPacket?: boolean
}

/**
 * # MuView Frontend
 *
 * ## MuHTTPClient Base
 *
 * The Mu's Customized HttpClient with Dual-Message Mode (JSON & MuPacket) based on Axios
 *
 * **Attention: Please use MuHttpClient, do not using this**
 *
 * @see MuHttpClient
 */
class MuHTTPClientBase{
    private readonly base: AxiosInstance

    constructor() {
        this.base = axios.create({
            baseURL: "http://127.0.0.1:20038",
            timeout: 30000,
            withCredentials: false,
            headers: { "Content-Type": "application/json", },
        })

        this.base.interceptors.request.use(
            (config) => {
                const muConfig = config as MuPacketAxiosConfig

                if (muConfig.usingMuPacket === true) {
                    if(config.data instanceof MuPacket){
                        config.data = config.data.toJSON()
                    }
                }

                return config
            },
            (err) => Promise.reject(err),
        )

        this.base.interceptors.response.use(
            (response) => {
                const muConfig = response.config as MuPacketAxiosConfig;

                if (muConfig.usingMuPacket === false) {
                    return response;
                }

                const data = response.data;

                if (this.isMuPacketData(data)) {
                    try {
                        response.data = MuPacketRegistry.parse(data);
                    } catch (e) {
                        console.warn(`[MuPacketAxios] 解析失败 (MP_ID=${data.MP_ID}):`, e);
                    }
                }

                return response;
            },
            (error) => Promise.reject(error)
        )
    }

    private isMuPacketData<T extends object>(data: unknown): data is MuPacketData<T> {
        return (
            data !== null &&
            typeof data === "object" &&
            "MP_ID" in data &&
            "MP_DATA" in data &&
            "CID" in data &&
            typeof (data as Record<string, unknown>).MP_ID === "string"
        );
    }

    private async request<T = unknown>(config: MuPacketAxiosConfig): Promise<T> {
        const res = await this.base.request<unknown>(config);
        return res.data as T;
    }

    public get<T = MuPacket>(api: string, config?: MuPacketAxiosConfig): Promise<T>{
        return this.request<T>({ ...config, method: 'GET', url: api })
    }

    public post<T = MuPacket>(api: string, mp: MuPacket, config?: MuPacketAxiosConfig): Promise<T>{
        return this.request<T>({ ...config, method: 'POST', url: api, data: mp.toJSON() })
    }

    public getRaw<T = object>(api: string, config?: MuPacketAxiosConfig): Promise<T> {
        return this.request<T>({ ...config, usingMuPacket: false, method: 'GET', url: api })
    }

    public postRaw<T = object>(api: string, data?: unknown, config?: MuPacketAxiosConfig): Promise<T> {
        return this.request<T>({ ...config, usingMuPacket: false, method: 'POST', url: api, data: data })
    }
}

/**
 * # MuView Frontend
 *
 * ## Mu WebSocket Connection Base - MuWSConnectionBase
 *
 * ### Attention: Unstabled Base!
 *
 * The Mu's Customized WebSocket, receive String Message or MuPacket (Optional).
 *
 * **Attention: Please use MuWSConnection, do not using this**
 *
 * @see MuWSConnection
 */
class MuWSConnectionBase{
    private readonly base: WebSocket
    private finalMSG: MuPacket | undefined
    private finalError: unknown
    private usingMuPacket: boolean

    constructor(api: string, usingMuPacket: boolean = true) {
        this.usingMuPacket = usingMuPacket
        this.base = new WebSocket(resolveWebSocketUrl(api))
        this.base.onopen = (e) => {
            console.log("WebSocket Connected >> " + e)
        }
        this.base.onerror = (e) => {
            console.log("Websocket Occurred an Error! >> " + e.type.toString())
            this.base.close()
        }
        this.base.onmessage = (e) => {
            try {
                if(usingMuPacket){
                    const rawMsg = JSON.parse(e.data)
                    this.finalMSG = this.isMuPacketData(rawMsg) ? MuPacketRegistry.parse(rawMsg) : rawMsg
                    this.finalError = undefined
                }else{
                    this.finalMSG = e.data
                    this.finalError = undefined
                }
            } catch (error) {
                this.finalError = error
                console.error(this.finalError)
                this.base.close(1003, "Closed Unexceptionally")
            }
        }
        this.base.onclose = (e) => {
            console.log("Websocket Closed! >> " + e.reason.toString())
        }
    }

    private isMuPacketData<T extends object>(data: unknown): data is MuPacketData<T> {
        return (
            data !== null &&
            typeof data === "object" &&
            "MP_ID" in data &&
            "MP_DATA" in data &&
            "CID" in data &&
            typeof (data as Record<string, unknown>).MP_ID === "string"
        );
    }

    public getMsg(): MuPacket | unknown {
        if (this.finalError != null) {
            throw this.finalError
        }

        if(this.isConnected()){
            if(this.usingMuPacket){
                return this.finalMSG as MuPacket
            }else{
                return this.finalMSG
            }
        }else{
            return undefined
        }
    }

    public isConnected(): boolean{
        return this.base.readyState == this.base.OPEN
    }

    public send(msg: any): unknown {
        if(this.base && this.isConnected()){
            if(this.usingMuPacket){
                const rawMsg = JSON.stringify(msg.toJSON())
                this.base.send(rawMsg)
                return this.getMsg()
            }else{
                this.base.send(msg)
                return this.getMsg()
            }
        }else{
            console.warn("Error occurred while send MSG to MuCore, probably MuCore OFFLINE")
        }
    }

    public close(){
        if(this.isConnected()){
            this.base.close()
        }
    }
}

/**
 * # MuView Frontend
 *
 * ## MuHTTPClient
 *
 * The Mu's Customized HttpClient with Dual-Message Mode (JSON & MuPacket) based on Axios
 */
export const MuHttpClient = new MuHTTPClientBase()

/**
 * # MuView Frontend
 *
 * ## Mu WebSocket Connection - MuWSConnection
 *
 * ### Attention: Unstabled!
 *
 * The Mu's Customized WebSocket, receive String Message or MuPacket (Optional).
 */
export const MuWSConnection=
    (api: string, usingMuPacket: boolean = true)=> new MuWSConnectionBase(api, usingMuPacket)
