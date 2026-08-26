<script setup lang="ts">

import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@shadcn/table";
import {
  columnFilteringFeature,
  columnVisibilityFeature,
  createColumnHelper,
  createFilteredRowModel,
  createPaginatedRowModel,
  createSortedRowModel,
  filterFn_includesString,
  rowExpandingFeature,
  rowPaginationFeature,
  rowSortingFeature,
  sortFn_alphanumeric,
  sortFn_text,
  tableFeatures,
  useTable,
  FlexRender,
  createCoreRowModel,
} from "@tanstack/vue-table";
import {h} from "vue";
import {Button} from "@shadcn/button";
import {Input} from "@shadcn/input";
import {refreshMuEnvList, MuEnvList, type MuEnv} from "@view/muenv/muenv.ts";
import EnvTableRowMenu from "@view/muenv/EnvTableRowMenu.vue";
import EnvImporter from "@view/muenv/EnvImporter.vue";
import {useI18n} from "vue-i18n";

const features = tableFeatures({
  columnFilteringFeature,
  columnVisibilityFeature,
  rowExpandingFeature,
  rowPaginationFeature,
  rowSortingFeature,
  coreRowModel: createCoreRowModel(),
  filteredRowModel: createFilteredRowModel(),
  paginatedRowModel: createPaginatedRowModel(),
  sortedRowModel: createSortedRowModel(),
  filterFns: {includesString: filterFn_includesString},
  sortFns: {alphanumeric: sortFn_alphanumeric, text: sortFn_text},
})

const columnsBase = createColumnHelper<typeof features, MuEnv>()
const {t} = useI18n()

// Environment Table Columns Definition
const columns = columnsBase.columns([
    columnsBase.accessor("EV_NAME", {
      header: () => h('div', { class: "text-left" }, t("muenv.table.title.name")),
      cell: ({row}) => h('div', { class: "text-left text-md" }, row.getValue('EV_NAME'))
    }),
    columnsBase.accessor("EV_VER", {
      header: () => h('div', { class: "text-left" }, t("muenv.table.title.version")),
      cell: ({row}) => h('div', { class: "text-left text-md" }, row.getValue('EV_VER'))
    }),
    columnsBase.display({
      id: 'actions',
      enableHiding: false,
      cell: ({ row }) => {
        const env = row.original
        return h(EnvTableRowMenu, {env: env.EV_NAME})
      },
    })
])

refreshMuEnvList()

const table = useTable({
  features,
  get data(){ return MuEnvList.value },
  get columns(){ return columns },
})

</script>

<template>
  <div class="flex flex-col gap-5 w-full">
    <blockquote class="border-l-2 border-black dark:border-white pl-3 font-bold">
      {{t("muenv.title")}}
    </blockquote>
    <div class="flex items-center py-4">
      <Input class="max-w-sm mr-auto" :placeholder="t('muenv.filterPlaceholder')"
             :model-value="table.getColumn('EV_NAME')?.getFilterValue() as string"
             @update:model-value="table.getColumn('EV_NAME')?.setFilterValue($event)" />
      <div class="flex flex-row gap-2 justify-end">
        <EnvImporter/>
      </div>
    </div>
    <Table>
      <TableHeader>
        <TableRow v-for="headerGroup in table.getHeaderGroups()" :key="headerGroup.id">
          <TableHead v-for="header in headerGroup.headers"
                     :key="header.id"
                     :style="header.column.columnDef.meta?.width? { width: header.column.columnDef.meta.width + 'px', minWidth: header.column.columnDef.meta.width + 'px' }: undefined"
          >
            <FlexRender
                v-if="!header.isPlaceholder" :render="header.column.columnDef.header"
                :props="header.getContext()"
            />
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <template v-if="table.getRowModel().rows?.length">
          <TableRow
              v-for="row in table.getRowModel().rows"
              :key="(row.original as MuEnv).EV_NAME"
          >
            <TableCell v-for="cell in row.getVisibleCells()" :key="cell.id">
              <FlexRender :render="cell.column.columnDef.cell" :props="cell.getContext()" />
            </TableCell>
          </TableRow>
        </template>
        <template v-else>
          <TableRow>
            <TableCell :colspan="columns.length" class="h-24 text-center">
              No results.
            </TableCell>
          </TableRow>
        </template>
      </TableBody>
    </Table>
    <div class="flex items-center justify-end space-x-2 py-4">
      <div class="space-x-2">
        <Button
            variant="outline"
            size="sm"
            :disabled="!table.getCanPreviousPage()"
            @click="table.previousPage()"
        >
          {{ t("muenv.table.page.previous") }}
        </Button>
        <Button
            variant="outline"
            size="sm"
            :disabled="!table.getCanNextPage()"
            @click="table.nextPage()"
        >
          {{ t("muenv.table.page.next") }}
        </Button>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>