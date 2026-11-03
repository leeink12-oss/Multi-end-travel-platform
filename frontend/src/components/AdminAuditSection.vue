<template>
  <el-card>
    <div class="section-header">
      <h3>{{ title }}</h3>
      <slot name="extra" />
    </div>
    <el-table :data="items" v-loading="loading">
      <template v-for="column in columns" :key="column.key">
        <el-table-column v-bind="column.props">
          <template v-if="column.render" #default="scope">
            <component :is="column.render(scope)" />
          </template>
        </el-table-column>
      </template>
      <el-table-column v-if="$slots.actions" label="操作" :width="actionsWidth">
        <template #default="scope">
          <slot name="actions" :row="scope.row" />
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  items: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  actionsWidth: { type: [String, Number], default: 220 },
});
</script>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
