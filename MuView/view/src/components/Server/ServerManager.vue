<script setup lang="ts">
import "element-plus/es/components/notification/style/css";
import "element-plus/es/components/message-box/style/css";
import {
  type ComponentSize,
  ElMessageBox,
  ElNotification,
  type FormInstance,
  type FormRules,
} from "element-plus";
import { h, onMounted, reactive, ref, } from "vue";
import { apiClient } from "@api/MuCoreConnector";
import { SERVER_LIST, getServers} from "@api/MuServer";
import { ENV_LIST } from "@api/MuEnvironment";

onMounted(() => {
  getServers()
  apiClient.get("api/v1/server/availableType").then(res => AvailableMCSType = res.data)
})

let AvailableMCSType = ref()
const AvailableMCSVersionLoading = ref(false)
const AvailableMCSVersion = ref([])
const AvailableJVMFlagsTemplate = ref<Array<{name: String, flags: String}>>([])

const fetchJVMFlagsTemplates  = () =>{
  apiClient.get("/api/v1/server/jvmFlagTemplates").then(res => AvailableJVMFlagsTemplate.value = res.data )
}

const fetchMCSVersionList = (api: string) => {
  ServerFormData.version = ''
  AvailableMCSVersionLoading.value = true
  apiClient.get(api).then(res => {
    AvailableMCSVersion.value = res.data.versions.reverse()
    AvailableMCSVersionLoading.value = false
  })
}

let onCreate = ref(false)
let onImport = ref(false)

interface ServerFormTemplate{
  name: string
  type: string
  port: number
  desc: string
  env: string
  version: string
  online: boolean
  whitelist: boolean
  max_player: number
  view_distance: number
  allow_nether: boolean
  spawn_protect: number
  jvm_flag_template: string
  jvm_aflags: string
  allow_gui: boolean
  minimum_mem: number
  maximum_mem: number
  before_works: { key: number, value: string }[]
}

const ServerFormRules = reactive<FormRules<ServerFormTemplate>>({
  name: [
    { required: true, message: 'Please input Correct name', trigger: 'blur' },
    { min: 5, max: 20, message: 'Please input Correct Name', trigger: 'blur' }
  ],
  type: [
    { required: true, message: 'Please select Activity zone', trigger: 'change' },
  ],
  port: [
    { required: true, message: 'Please input Correct name', trigger: 'blur' }
  ],
  env: [
    { required: true, message: "Please Select a Valid Environment", trigger: "change" }
  ],
  version: [
    { required: true, message: "Please Select a Valid MC Server Version", trigger: "blur" },
  ],
  jvm_flag_template: [
    { required: true, message: "Please Select a Valid Flag", trigger: "blur" },
  ],
  max_player: [
    { required: true, message: "Please Select a Valid MC Server Max Player", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value <= 0){
            callback(new Error('Do not set this under the 1'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur'},
  ],
  view_distance: [
    { required: true, message: "Please Select a Valid MC Server View Distance", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value < 4 || value > 10){
            callback(new Error('Do not set this in 4~10'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur' },
  ],
  spawn_protect: [
    { required: true, message: "Please Select a Valid MC Server Spawn Protect Range", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value < 0){
            callback(new Error('Do not set this under the 0'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur'},
  ],
  maximum_mem: [
    { required: true, message: "Please input a Valid Number", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value < ServerFormData.minimum_mem){
            callback(new Error('Do not set this less than Minimum Value'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur'},
  ],
  minimum_mem: [
    { required: true, message: "Please input a Valid Number", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value < 512){
            callback(new Error('Do not set this under the 512'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur'},
  ],
})

const ServerFormData = reactive<ServerFormTemplate>({
  name: '',
  type: '',
  port: 25565,
  desc: '',
  env: '',
  version: '',
  online: true,
  whitelist: false,
  max_player: 20,
  view_distance: 10,
  allow_nether: true,
  spawn_protect: 10,
  jvm_flag_template: 'none',
  jvm_aflags: '',
  allow_gui: false,
  minimum_mem: 512,
  maximum_mem: 512,
  before_works: [],
})

const ServerFormSize = ref<ComponentSize>('default')
const ServerFormRef = ref<FormInstance>()

const submitCreateForm = async (form: FormInstance | undefined, data: any) => {
  if(!form) return
  await form.validate((v, f) => {
    if (v) {
      sendCreateServerRequest(data).then(res => {
        if(res){
          console.log("submit!")
          ElNotification({
            title: 'Create Success',
            type: 'success',
            duration: 5000,
            offset: 100,
          })
          onCreate.value = false
        }else{
          console.log("Handled Error!")
        }
      })
    } else {
      console.log('error submit!', f)
    }
  })
}

const sendCreateServerRequest = async (data: any): Promise<boolean> => {
  return apiClient.post(`/api/v1/server/create`, data)
      .then(r => r.status === 200)
      .catch(e => {
        ElNotification({
          title: 'Create Error',
          message: e.response.data,
          type: 'error',
          duration: 5000,
          offset: 100,
        })
        return false
      }).finally(() => {
        getServers()
      })
}

const cancelCreateServer = () => {
  onCreate.value = false
  ServerFormData.name = ''
  ServerFormData.type = ''
  ServerFormData.port = 25565
  ServerFormData.desc = ''
  ServerFormData.env = ''
  ServerFormData.version = ''
  ServerFormData.online = true
  ServerFormData.whitelist = false
  ServerFormData.max_player = 20
  ServerFormData.view_distance = 10
  ServerFormData.allow_nether = true
  ServerFormData.spawn_protect = 10
  ServerFormData.jvm_flag_template = 'none'
  ServerFormData.jvm_aflags = ''
  ServerFormData.allow_gui = false
  ServerFormData.minimum_mem = 512
  ServerFormData.maximum_mem = 51
  ServerFormData.before_works = []
}

const sendDeleteServerRequest = (target: string) => {
  ElMessageBox.confirm(
      "Will Delete this Server Permanently(really permanently), Continue?",
      "Confirm?",
      {
        confirmButtonText: 'Confirm Delete',
        cancelButtonText: 'I regret',
        type: "warning"
      }
  ).then(() => {
    apiClient.get(`/api/v1/server/delete/${target}`).then( r => {
      if(r.status === 200){
        ElNotification({
          type: 'success',
          title: 'Delete completed',
          duration: 5000,
          offset: 100
        })
      }}
    ).catch(e =>
        ElNotification({
          type: 'error',
          title: 'Occurred a Exception when Delete Server',
          message: e.response.data,
          duration: 5000,
          offset: 100
    })).finally(() => getServers())
  })
}

const sendRemoveServerRequest = (target: string) => {
  ElMessageBox.confirm(
      h('div', null, [
          h('p', null, 'Will Remove this server, Confirm'),
          h('p', { class: 'text-green-400' }, 'It can be import when you want to reuse this server'),
      ]),
      "Confirm?",
      {
        confirmButtonText: 'Confirm Remove',
        cancelButtonText: 'I regret',
        type: "warning"
      }
  ).then(() => {
    apiClient.get(`/api/v1/server/remove/${target}`).then( r => {
      if(r.status === 200){
        ElNotification({
          type: 'success',
          title: 'Remove completed',
          duration: 5000,
          offset: 100
        })
      }}
    ).catch(e => ElNotification({
      type: 'error',
      title: 'Occurred a Exception when Remove Server',
      message: e.response.data,
      duration: 5000,
      offset: 100
    })).finally(() => getServers())
  })
}

const addBeforeWork = () => {
  ServerFormData.before_works.push({
    key: Date.now(),
    value: ''
  })
}

const removeBeforeWork = (i: {key: number, value: string}) => {
  const index = ServerFormData.before_works.indexOf(i)
  if(index !== -1){
    ServerFormData.before_works.splice(index, 1)
  }
}

interface ServerImportForm{
  name: string,
  desc: string,
  path: string,
  port: number,
  env: string,
  jvm_flag_template: string,
  jvm_aflags: string,
  allow_gui: boolean,
  minimum_mem: number,
  maximum_mem: number,
  before_works: { key: number, value: string }[],
}

const ServerImportFormData = reactive<ServerImportForm>({
  name: '',
  port: 25565,
  desc: '',
  path: '',
  env: '',
  jvm_flag_template: 'none',
  jvm_aflags: '',
  allow_gui: false,
  minimum_mem: 512,
  maximum_mem: 512,
  before_works: [],
})

const ServerImportFormRules = reactive<FormRules<ServerImportForm>>({
  name: [
    { required: true, message: "Please input a valid name", trigger: 'blur' }
  ],
  path: [
    { required: true, message: "Please input a valid name", trigger: 'blur' }
  ],
  port: [
    { required: true, message: 'Please input Correct name', trigger: 'blur' }
  ],
  env: [
    { required: true, message: "Please Select a Valid Environment", trigger: "change" }
  ],
  jvm_flag_template: [
    { required: true, message: "Please Select a Valid Flag", trigger: "blur" },
  ],
  maximum_mem: [
    { required: true, message: "Please input a Valid Number", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value < ServerImportFormData.minimum_mem){
            callback(new Error('Do not set this less than Minimum Value'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur'},
  ],
  minimum_mem: [
    { required: true, message: "Please input a Valid Number", trigger: "blur" },
    { validator: (_, value, callback) => {
        if(!Number.isInteger(value)){
          callback(new Error('Please input a Number'))
        }else{
          if(value < 512){
            callback(new Error('Do not set this under the 512'))
          }else{
            callback()
          }
        }
      }, trigger: 'blur'},
  ],
})

const ServerImportFormRef = ref<FormInstance>()

const submitImportForm = async (form: FormInstance | undefined, data: any) => {
  if(!form) return
  await form.validate((v, f) => {
    if (v) {
      sendImportServerRequest(data).then(res => {
        if(res){
          console.log("submit!")
          ElNotification({
            title: 'Create Success',
            type: 'success',
            duration: 5000,
            offset: 100,
          })
          onImport.value = false
        }else{
          console.log("Handled Error!")
        }
      })
    } else {
      console.log('error submit!', f)
    }
  })
}

const sendImportServerRequest = async (data: any): Promise<boolean> => {
  return apiClient.post(`/api/v1/server/import`, data)
      .then(r => r.status === 200)
      .catch(e => {
        ElNotification({
          title: 'Import Error',
          message: e.response.data,
          type: 'error',
          duration: 5000,
          offset: 100,
        })
        return false
      }).finally(() => {
        getServers()
      })
}

const cancelImportServer = () => {
  onImport.value = false
  ServerImportFormData.name =  ''
  ServerImportFormData.port =  25565
  ServerImportFormData.desc =  ''
  ServerImportFormData.path =  ''
  ServerImportFormData.env =  ''
  ServerImportFormData.jvm_flag_template = 'none'
  ServerImportFormData.jvm_aflags =  ''
  ServerImportFormData.allow_gui =  false
  ServerImportFormData.minimum_mem = 512
  ServerImportFormData.maximum_mem = 512
  ServerImportFormData.before_works =  []
}

</script>

<template>
  <el-space fill class="w-full">
    <el-card>
      <el-button size="default" type="success" @click.capture="onCreate = true">
        <el-icon class="mr-2" size="15">
          <svg class="stroke-2 stroke-white" fill="none" stroke-linecap="round" stroke-linejoin="round"
               viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <line x1="12" x2="12" y1="5" y2="19"/>
            <line x1="5" x2="19" y1="12" y2="12"/>
          </svg>
        </el-icon>
        Create
      </el-button>
      <el-button size="default" type="success" @click.capture="onImport = true">
        <el-icon class="mr-2" size="15">
          <svg class="stroke-2 stroke-white" fill="none" stroke-linecap="round" stroke-linejoin="round"
               viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" x2="12" y1="15" y2="3"/>
          </svg>
        </el-icon>
        Import
      </el-button>
      <el-table :data="SERVER_LIST" stripe>
        <el-table-column prop="name" label="Name" min-width="80">
          <template #default="scope">
            <router-link :to="`/server/${scope.row.name}`">
              <span class="text-[14px] hover:text-blue-400 underline">{{ scope.row.name }}</span>
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="type.name" label="Type" min-width="100"/>
        <el-table-column prop="version" label="Version" min-width="60"/>
        <el-table-column prop="running" label="Actions" align="right" min-width="150">
          <template #header>
            <!--          <el-input v-model="search" size="default" placeholder="Type to search"/>-->
          </template>
          <template #default="scope">
            <el-button size="small" :type="scope.row.running ? 'warning' : 'success'"
                       v-text="scope.row.running ? 'Stop' : 'Start'"/>
            <el-popover trigger="click" width="30">
              <template #reference>
                <el-button size="small" type="danger">Delete</el-button>
              </template>
              <el-space direction="vertical" fill class="w-full">
                <el-button size="small" type="danger" @click.prevent="sendDeleteServerRequest(scope.row.name)">Delete
                </el-button>
                <el-button size="small" type="warning" @click.prevent="sendRemoveServerRequest(scope.row.name)">Remove
                </el-button>
              </el-space>
            </el-popover>
          </template>
        </el-table-column>
      </el-table>

      <!--  Server Create Dialog  -->
      <el-dialog
          v-model="onCreate"
          title="Create Server"
          width="600"
          align-center
      >
        <el-scrollbar
            height="800"
            class="p-4"
        >
          <el-form
              ref="ServerFormRef"
              :model="ServerFormData"
              :rules="ServerFormRules"
              :size="ServerFormSize"
              label-position="top"
              status-icon
          >
            <span class="text-lg font-bold">| Base</span>
            <el-form-item prop="name">
              <template #label><span class="text-base">Name</span></template>
              <el-input
                  minlength="5"
                  maxlength="20"
                  show-word-limit
                  v-model="ServerFormData.name"
                  placeholder="Length limit in 5~20"/>
            </el-form-item>
            <el-form-item prop="port">
              <template #label><span class="text-base">Port</span></template>
              <el-input
                  min="1024"
                  max="65534"
                  type="number"
                  v-model="ServerFormData.port"
                  placeholder="input a number in 1024~65534"/>
            </el-form-item>
            <el-form-item>
              <template #label><span class="text-base">Description</span></template>
              <el-input type="textarea" v-model="ServerFormData.desc"/>
            </el-form-item>
            <el-form-item prop="env">
              <template #label><span class="text-base">Environment</span></template>
              <el-select v-model="ServerFormData.env" :disabled="ENV_LIST.length === 0">
                <el-option v-for="i in ENV_LIST"
                           :key="i.name"
                           :label="`${i.name} (${i.version})`"
                           :value="i.name"
                />
              </el-select>
              <span v-show="ENV_LIST.length === 0" class="text-sm text-red-500">You not have any Environment! Import First.</span>
            </el-form-item>
            <el-form-item prop="type">
              <template #label><span class="text-base">Minecraft Server Type</span></template>
              <el-select v-model="ServerFormData.type">
                <el-option v-for="a in AvailableMCSType" :label="a.name" :value="a.id"
                           @click.capture="fetchMCSVersionList(a.api)">
                  <span class="float-left">{{ a.name }}</span>
                  <span class="ml-5 float-right text-(--el-text-color-secondary) text-[13px]">{{ a.desc }}</span>
                </el-option>
              </el-select>
              <span class="text-sm text-gray-400">Not listed you want? Attempt to <a class="underline"
                                                                                     @click="onCreate = false; onImport = true">Import Server</a> using Custom Type MC Server</span>
            </el-form-item>
            <el-form-item prop="version">
              <template #label><span class="text-base">Minecraft Server Version</span></template>
              <el-select :disabled="AvailableMCSVersion.length == 0" v-model="ServerFormData.version"
                         v-loading="AvailableMCSVersionLoading">
                <el-option v-for="a in AvailableMCSVersion" :label="a" :value="a"/>
              </el-select>
            </el-form-item>
            <span class="text-lg font-bold">| Startup</span><br/>
            <el-space class="w-full mt-3">
              <el-tooltip placement="right">
                <template #content><span class="text-green-500">Tip: 1024M = 1G</span></template>
                <span class="test-base mr-5">Memory<sup>*</sup></span>
              </el-tooltip>
              <el-form-item prop="minimum_mem">
                <template #label><span class="text-base">Minimum</span></template>
                <el-input-number style="width: 180px;" v-model="ServerFormData.minimum_mem">
                  <template #suffix>M</template>
                </el-input-number>
              </el-form-item>
              <span class="text-lg font-bold mx-4">~</span>
              <el-form-item prop="maximum_mem">
                <template #label><span class="text-base">Maximum</span></template>
                <el-input-number style="width: 180px;" v-model="ServerFormData.maximum_mem">
                  <template #suffix>M</template>
                </el-input-number>
              </el-form-item>
            </el-space>
            <el-form-item>
              <el-tooltip placement="right">
                <template #content>
                  <span class="text-yellow-400">Warning! GUI Mode is not Necessary. Keep close is Recommended</span>
                </template>
                <span class="text-base mr-5">Show GUI<sup>*</sup></span>
              </el-tooltip>
              <el-switch v-model="ServerFormData.allow_gui"/>
            </el-form-item>
            <span class="text-base">Before Works</span>
            <el-form-item
                v-for="(v, index) in ServerFormData.before_works"
                :key="v.key"
                :label="`Work${index}: `"
                :prop="`before_works.${index}.value`"
                :rules="{
              required: true,
              message: 'Work dont set empty',
              trigger: 'blur',
            }"
                label-position="left"
            >
              <div class="flex flex-row w-full">
                <el-input v-model="v.value"></el-input>
                <el-button type="danger" class="mx-2" @click.prevent="removeBeforeWork(v)">Delete</el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button class="mt-2" @click.prevent="addBeforeWork">Add Work</el-button>
            </el-form-item>
            <span class="text-lg font-bold mb-5">| Configuration</span>
            <el-form-item>
              <el-space :size="30" direction="horizontal">
                <span class="text-sm">Online Server <el-switch v-model="ServerFormData.online"/></span>
                <span class="text-sm">White List <el-switch v-model="ServerFormData.whitelist"/></span>
                <span class="text-sm">Allow Nether <el-switch v-model="ServerFormData.allow_nether"/></span>
              </el-space>
            </el-form-item>
            <el-form-item prop="max_player">
              <template #label><span class="text-base">Max Player</span></template>
              <el-input type="number" v-model="ServerFormData.max_player"/>
            </el-form-item>
            <el-form-item prop="view_distance">
              <template #label><span class="text-base">View Distance</span></template>
              <el-input type="number" v-model="ServerFormData.view_distance"/>
            </el-form-item>
            <el-form-item prop="spawn_protect">
              <template #label><span class="text-base">Spawn Protect Range</span></template>
              <el-input type="number" v-model="ServerFormData.spawn_protect"/>
            </el-form-item>
            <div class="mb-5">
              <div class="text-lg font-bold">| Advance Settings</div>
              <el-text type="danger" tag="b">Do not change if you not clear the usage about this setting.</el-text>
            </div>
            <el-form-item prop="jvm_flag_template">
              <template #label><span class="text-base">JVM Startup Flags</span></template>
              <el-select v-model="ServerFormData.jvm_flag_template"
                         @change="(value: any) => { ServerFormData.jvm_aflags = AvailableJVMFlagsTemplate.find((v) => v.flags == value)?.flags as string }">
                <el-option v-for="i in AvailableJVMFlagsTemplate" :value="i.flags" :label="i.name"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <template #label><span class="text-base">Additional JVM Args</span></template>
              <el-input :disabled="ServerFormData.jvm_flag_template != 'custom'" type="textarea"
                        v-model="ServerFormData.jvm_aflags"/>
            </el-form-item>
          </el-form>
        </el-scrollbar>
        <div class="w-full p-4">
          <div class="float-end">
            <el-link class="mr-3" type="primary" @click.prevent="cancelCreateServer">Cancel</el-link>
            <el-button type="primary" @click.prevent="submitCreateForm(ServerFormRef, ServerFormData)">Create
            </el-button>
          </div>
        </div>
      </el-dialog>

      <!--  Server Import Dialog  -->
      <el-dialog
          v-model="onImport"
          title="Import Server"
          width="600"
          align-center
      >
        <el-scrollbar max-height="800" class="p-4">
          <el-form
              ref="ServerImportFormRef"
              :model="ServerImportFormData"
              :rules="ServerImportFormRules"
              :size="'default'"
              status-icon
              label-position="top"
          >
            <span class="text-lg font-bold">| Base</span>
            <el-form-item prop="name">
              <template #label><span class="text-base">Name</span></template>
              <el-input
                  minlength="5"
                  maxlength="20"
                  show-word-limit
                  v-model="ServerImportFormData.name"
                  placeholder="Length limit in 5~20"/>
            </el-form-item>
            <el-form-item prop="path">
              <template #label><span class="text-base">Path</span></template>
              <el-input
                  v-model="ServerImportFormData.path"
                  placeholder="Input the server jar file path"/>
            </el-form-item>
            <el-form-item prop="port">
              <template #label><span class="text-base">Port</span></template>
              <el-input
                  min="1024"
                  max="65534"
                  type="number"
                  v-model="ServerImportFormData.port"
                  placeholder="input a number in 1024~65534"/>
            </el-form-item>
            <el-form-item>
              <template #label><span class="text-base">Description</span></template>
              <el-input type="textarea" v-model="ServerImportFormData.desc"/>
            </el-form-item>
            <el-form-item prop="env">
              <template #label><span class="text-base">Environment</span></template>
              <el-select v-model="ServerImportFormData.env" :disabled="ENV_LIST.length === 0">
                <el-option v-for="i in ENV_LIST"
                           :key="i.name"
                           :label="`${i.name} (${i.version})`"
                           :value="i.name"
                />
              </el-select>
              <span v-show="ENV_LIST.length === 0" class="text-sm text-red-500">You not have any Environment! Import First.</span>
            </el-form-item>
            <span class="text-lg font-bold">| Startup</span><br/>
            <el-space class="w-full mt-3">
              <el-tooltip placement="right">
                <template #content><span class="text-green-500">Tip: 1024M = 1G</span></template>
                <span class="test-base mr-5">Memory<sup>*</sup></span>
              </el-tooltip>
              <el-form-item prop="minimum_mem">
                <template #label><span class="text-base">Minimum</span></template>
                <el-input-number style="width: 180px;" v-model="ServerImportFormData.minimum_mem">
                  <template #suffix>M</template>
                </el-input-number>
              </el-form-item>
              <span class="text-lg font-bold mx-4">~</span>
              <el-form-item prop="maximum_mem">
                <template #label><span class="text-base">Maximum</span></template>
                <el-input-number style="width: 180px;" v-model="ServerImportFormData.maximum_mem">
                  <template #suffix>M</template>
                </el-input-number>
              </el-form-item>
            </el-space>
            <el-form-item>
              <el-tooltip placement="right">
                <template #content>
                  <span class="text-yellow-400">Warning! GUI Mode is not Necessary. Keep close is Recommended</span>
                </template>
                <span class="text-base mr-5">Show GUI<sup>*</sup></span>
              </el-tooltip>
              <el-switch v-model="ServerImportFormData.allow_gui"/>
            </el-form-item>
            <span class="text-base">Before Works</span>
            <el-form-item
                v-for="(v, index) in ServerImportFormData.before_works"
                :key="v.key"
                :label="`Work${index}: `"
                :prop="`before_works.${index}.value`"
                :rules="{ required: true, message: 'Work dont set empty', trigger: 'blur', }"
                label-position="left"
            >
              <div class="flex flex-row w-full">
                <el-input v-model="v.value"></el-input>
                <el-button type="danger" class="mx-2" @click.prevent="removeBeforeWork(v)">Delete</el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button class="mt-2" @click.prevent="addBeforeWork">Add Work</el-button>
            </el-form-item>
            <div class="mb-5">
              <div class="text-lg font-bold">| Advance Settings</div>
              <el-text type="danger" tag="b">Do not change if you not clear the usage about this setting.</el-text>
            </div>
            <el-form-item prop="jvm_flag_template">
              <template #label><span class="text-base">JVM Startup Flags</span></template>
              <el-select v-model="ServerImportFormData.jvm_flag_template"
                         @change="(value: any) => { ServerImportFormData.jvm_aflags = AvailableJVMFlagsTemplate.find((v) => v.flags == value)?.flags as string }">
                <el-option v-for="i in AvailableJVMFlagsTemplate" :value="i.flags" :label="i.name"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <template #label><span class="text-base">Additional JVM Args</span></template>
              <el-input :disabled="ServerImportFormData.jvm_flag_template != 'custom'" type="textarea"
                        v-model="ServerImportFormData.jvm_aflags"/>
            </el-form-item>
          </el-form>
        </el-scrollbar>
        <template #footer>
          <el-button @click="cancelImportServer">Cancel</el-button>
          <el-button type="primary" @click="submitImportForm(ServerImportFormRef, ServerImportFormData)">Confirm
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </el-space>
</template>