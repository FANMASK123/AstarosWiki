<template>
  <div>
    <div style="display:flex;gap:0;border-bottom:1px solid var(--border);margin-bottom:20px">
      <router-link to="/pages/create" class="btn btn-ghost" style="border-bottom:none;font-size:12px" :style="{fontWeight:$route.path==='/pages/create'?'700':'400'}">创建</router-link>
      <router-link to="/pages/edit" class="btn btn-ghost" style="border-bottom:none;font-size:12px" :style="{fontWeight:$route.path==='/pages/edit'?'700':'400'}">编辑</router-link>
      <router-link to="/pages/manage" class="btn btn-ghost" style="border-bottom:none;font-size:12px" :style="{fontWeight:$route.path==='/pages/manage'?'700':'400'}">发布管理</router-link>
    </div>
    <router-view />
  </div>
</template>
