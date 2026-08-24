<script setup>
defineProps({ size: { type: String, default: 'text-[21px]' }, light: Boolean });
</script>

<template>
    <span class="inline-flex items-baseline font-extrabold leading-none tracking-[-0.035em]" :class="size">
        <span :class="light ? 'text-white' : 'text-navy'">Data</span><span class="text-sky">Insights</span>
    </span>
</template>
