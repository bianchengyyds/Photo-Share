<!-- 图片预览（灯箱 / Lightbox）子组件 -->
<!-- 后续要在图片预览的时候在图片下方添加 当前页码/总页数, 放大/缩小按钮, 适应屏幕等 -->

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 bg-black/90 flex items-center justify-center" @click.self="close">
    <!-- 关闭按钮 -->
    <button
      class="absolute top-4 right-4 text-white hover:text-gray-300 z-10"
      @click="close"
    >
      <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>

    <!-- 上一张按钮, 仅在当前图片不是第一张时显示 -->
    <button
      v-if="currentIndex > 0"
      class="absolute left-4 top-1/2 -translate-y-1/2 text-white hover:text-gray-300 z-10"
      @click="prev"
    >
      <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
    </button>

    <!-- 下一张按钮, 仅在当前图片不是最后一张时显示 -->
    <button
      v-if="currentIndex < images.length - 1"
      class="absolute right-4 top-1/2 -translate-y-1/2 text-white hover:text-gray-300 z-10"
      @click="next"
    >
      <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>

    <img
      :src="currentImage"
      class="max-w-[90vw] max-h-[90vh] object-contain"
      @click.stop
    />
  </div>
</template>


<script setup lang="ts">
import { computed, watch } from 'vue'

const props = defineProps<{
  isOpen: boolean
  images: string[]
  initialIndex: number
}>()

// 声明组件能向外抛出的自定义事件。
const emit = defineEmits<{
  close: []
}>()

// 这是 Vue 3.4+ 的语法糖，相当于同时做了两件事：
//  内部拿到一个可写的 ref currentIndex；
//  外部父组件可以用 v-model:index="xxx" 双向绑定。
const currentIndex = defineModel<number>('index', { default: 0 })


// 根据下标算出当前该显示哪张图
const currentImage = computed(() => props.images[currentIndex.value] || '')

// 当父组件传入的“起始下标”变化时（比如从列表另一张图重新打开预览），把内部的浏览进度重置到那个位置。
watch(() => props.initialIndex, (val) => {
  currentIndex.value = val
})

// 把「关闭」这个动作，封装成一个自定义事件，通知父组件, 由监听这个事件的人（通常是父组件）来决定怎么处理。
function close() {
  emit('close')
}

function prev() {
  // 如果当前图片是第一张, 则不显示
  if (currentIndex.value > 0) {
    currentIndex.value-- // 切换到上一张图片, 即索引减1
  }
}

function next() {
  // 如果当前图片是最后一张, 则不显示
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++ // 切换到下一张图片, 即索引加1
  }
}
</script>
