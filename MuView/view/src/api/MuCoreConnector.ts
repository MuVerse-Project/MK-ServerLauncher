import axios, {type AxiosInstance, type AxiosRequestConfig} from "axios";
import {MuPacketReader, MuPacketReadError} from "@muapi/mupacket/MuPacketReader.ts";
import {MuPacketSender} from "@muapi/mupacket/MuPacketSender.ts";
import "@muapi/mupacket/MuMsgPacket.ts";

const resolveIncomingPayload = (raw: unknown): unknown => {
    const parsed = MuPacketReader.parseJSON(raw)
    return MuPacketReader.isPacketShape(parsed)
        ? MuPacketReader.read(parsed)
        : parsed
}

const resolveOutgoingPayload = (data: unknown): unknown => {
    return MuPacketSender.packIfMuPacket(data)
}

const resolveWebSocketUrl = (api: string) => {
    if (/^wss?:\/\//i.test(api)) {
        return api
    }

    const protocol = window.location.protocol === "https:" ? "wss" : "ws"
    const path = api.startsWith("/") ? api : `/${api}`
    return `${protocol}://${window.location.host}${path}`
}

class MuHTTPClientBase{
    private readonly base: AxiosInstance

    constructor() {
        this.base = axios.create({
            timeout: 30000,
            withCredentials: true,
            headers: { "Content-Type": "application/json", },
        })

        this.base.interceptors.request.use(
            (config) => { return config },
            (err) => Promise.reject(err),
        )

        this.base.interceptors.response.use(
            (res) => {
                res.data = resolveIncomingPayload(res.data)
                return res
            },
            (err) => Promise.reject(err),
        )
    }

    private async request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
        const res = await this.base.request<unknown>(config);
        return res.data as T;
    }

    public get<T = unknown>(api: string, config?: AxiosRequestConfig): Promise<T>{
        return this.request<T>({ ...config, method: 'GET', url: api })
    }

    public post<T = unknown>(api: string, data: unknown, config?: AxiosRequestConfig): Promise<T>{
        return this.request<T>({ ...config, method: 'POST', url: api, data: resolveOutgoingPayload(data) })
    }

    public put<T = unknown>(api: string, data: unknown, config?: AxiosRequestConfig): Promise<T>{
        return this.request<T>({ ...config, method: 'PUT', url: api, data: resolveOutgoingPayload(data) })
    }

    public delete<T = unknown>(api: string, config?: AxiosRequestConfig): Promise<T>{
        return this.request<T>({ ...config, method: 'DELETE', url: api })
    }
}

class MuWSConnectionBase{
    private readonly base: WebSocket
    private finalMSG: unknown
    private finalError: Error | undefined

    constructor(api: string) {
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
                this.finalMSG = resolveIncomingPayload(e.data)
                this.finalError = undefined
            } catch (error) {
                this.finalError = error instanceof Error
                    ? error
                    : new MuPacketReadError(String(error), e.data)
                console.error(this.finalError)
                this.base.close(1003, this.finalError.message)
            }
        }
        this.base.onclose = (e) => {
            console.log("Websocket Closed! >> " + e.reason.toString())
        }
    }

    public getMsg(): unknown {
        if (this.finalError != null) {
            throw this.finalError
        }

        if(this.isConnected()){
            return this.finalMSG
        }else{
            return undefined
        }
    }

    public isConnected(): boolean{
        return this.base.readyState == this.base.OPEN
    }

    public send(jsonMsg: unknown): unknown {
        if(this.base && this.isConnected()){
            this.base.send(JSON.stringify(resolveOutgoingPayload(jsonMsg)))
            return this.getMsg()
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

export const MuHttpClient = new MuHTTPClientBase()
export const MuWSConnection=
    (api: string)=> new MuWSConnectionBase(api)
