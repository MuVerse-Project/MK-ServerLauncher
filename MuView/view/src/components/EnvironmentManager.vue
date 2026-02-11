<script setup lang="ts">
import {onMounted, reactive, ref} from "vue"
import {apiClient} from "@api/MuCoreConnector";
import {
  type ComponentSize,
  ElNotification,
  type FormInstance,
  type FormRules
} from "element-plus";
import { ENV_LIST, getEnvs } from "@api/MuEnvironment";
let onImport = ref(false)

onMounted(() => getEnvs())

interface EnvFormTemplate{
  name: string
  path: string
}

const EnvFormRules = reactive<FormRules<EnvFormTemplate>>({
  name:[
    {required: true}
  ],
  path:[
    {required: true}
  ]
})

const EnvFormData = reactive<EnvFormTemplate>({
  name: '',
  path: '',
})

const EnvFormSize = ref<ComponentSize>('default')

const EnvFormRef = ref<FormInstance>()

const submitForm = async (form: FormInstance | undefined, data: EnvFormTemplate) => {
  if(!form) return
  await form.validate((v, f) => {
    if (v) {
      sendCreateEnvRequest(data).then(res => {
        if(res){
          console.log("submit!")
          ElNotification({
            title: 'Import Success!',
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

const sendCreateEnvRequest = async (form: any): Promise<boolean> => {
  return apiClient.post(`/api/v1/env/create`, form)
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
        getEnvs()
      })
}

const sendDeleteEnvRequest = async (index: number) => {
  apiClient.get(`api/v1/env/delete/${index}`)
      .then(r => {
        if(r.status === 200){
          ElNotification({
            title: 'Delete Success.',
            type: 'success',
            duration: 5000,
            offset: 100
          })
        }
      })
      .catch(e => {
        ElNotification({
          title: e.response.message,
          type: 'error',
          duration: 5000,
          offset: 100
        })
        return false
      }).finally(() => getEnvs())
}
</script>

<template>
  <el-space fill class="w-full">
    <el-card>
      <el-button size="default" type="success" @click.capture="onImport = true">
        <el-icon class="mr-2" size="15">
          <svg class="stroke-2 stroke-white" fill="none" stroke-linecap="round" stroke-linejoin="round"
               viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <line x1="12" x2="12" y1="5" y2="19"/>
            <line x1="5" x2="19" y1="12" y2="12"/>
          </svg>
        </el-icon>
        Import
      </el-button>
      <el-table :data="ENV_LIST" stripe>
        <el-table-column prop="name" label="Name" min-width="100"/>
        <el-table-column prop="version" label="Version" min-width="80"/>
        <el-table-column prop="path" label="Path" min-width="100"/>
        <el-table-column label="Actions" min-width="100" align="right">
          <template #default="scope">
            <el-button size="small" type="danger" @click="sendDeleteEnvRequest(scope.row.name)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog
      v-model="onImport"
      title="Import"
      width="500"
    >
      <el-form
        ref="EnvFormRef"
        :model="EnvFormData"
        :rules="EnvFormRules"
        :size="EnvFormSize"
        label-position="top"
      >
        <el-form-item prop="name">
          <template #label><span class="text-base">Name</span></template>
          <el-input v-model="EnvFormData.name"/>
        </el-form-item>
        <el-form-item prop="path">
          <template #label><span class="text-base">Path</span></template>
          <el-input v-model="EnvFormData.path" placeholder="Point to Java Installed Folder"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onImport = false">Cancel</el-button>
        <el-button type="primary" @click="submitForm(EnvFormRef, EnvFormData)">Confirm</el-button>
      </template>
    </el-dialog>
  </el-space>
</template>

<style scoped>

</style>