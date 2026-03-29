<template>
  <div class="app-container black-white-theme">
    <!-- 路由视图，显示当前路由对应的组件 -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}
</style>