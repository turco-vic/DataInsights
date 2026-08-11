<template>
    <div
        class="overflow-hidden rounded-[20px] border border-navy/10 bg-white shadow-[0_40px_80px_-50px_rgba(0,16,68,0.55)]">
        <div class="flex items-center justify-between border-b border-navy/10 px-[22px] py-4">
            <div class="flex items-center gap-2.5">
                <span class="block h-2.5 w-2.5 rounded-full bg-navy"></span>
                <span class="text-[13px] font-bold tracking-tight text-navy">Dashboard · Produção 2026</span>
            </div>
            <span
                class="rounded-md bg-sky/15 px-2.5 py-[5px] text-[11px] font-bold tracking-wider text-ocean">ATUALIZADO</span>
        </div>

        <div class="grid grid-cols-3 gap-px bg-navy/10">
            <div v-for="k in kpis" :key="k.label" class="bg-white px-5 py-[18px]">
                <div class="text-[11px] font-bold tracking-[0.06em] text-navy/50">{{ k.label }}</div>
                <div class="mt-1.5 text-[26px] font-extrabold tracking-tight"
                    :class="k.accent ? 'text-ocean' : 'text-navy'">{{ k.value }}</div>
            </div>
        </div>

        <div class="grid grid-cols-[1.55fr_1fr] items-end gap-6 px-[22px] pb-[26px] pt-6">
            <div>
                <div class="mb-[18px] text-xs font-bold tracking-wide text-navy/55">VOLUME POR MÊS</div>
                <div class="flex h-[132px] items-end gap-2.5">
                    <div v-for="b in bars" :key="b.mes" class="flex-1 rounded-t-[5px]" :class="b.color"
                        :style="{ height: b.h + '%' }"></div>
                </div>
                <div class="mt-2.5 flex gap-2.5">
                    <div v-for="b in bars" :key="b.mes"
                        class="flex-1 text-center text-[10px] font-semibold text-navy/40">{{ b.mes }}</div>
                </div>
            </div>
            <div>
                <div class="mb-[18px] text-xs font-bold tracking-wide text-navy/55">SETORES</div>
                <div class="mx-auto flex h-[118px] w-[118px] items-center justify-center rounded-full"
                    style="background: conic-gradient(#001044 0 46%, #004B85 46% 76%, #2E91C9 76% 92%, rgba(0,16,68,0.12) 92% 100%)">
                    <div
                        class="flex h-[68px] w-[68px] items-center justify-center rounded-full bg-white text-[15px] font-extrabold tracking-tight text-navy">
                        46%</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
const kpis = [
    { label: 'REGISTROS', value: '14.820' },
    { label: 'EFICIÊNCIA', value: '92,4%', accent: true },
    { label: 'PLANILHAS', value: '37' },
];
const bars = [
    { mes: 'JAN', h: 42, color: 'bg-navy/15' },
    { mes: 'FEV', h: 58, color: 'bg-navy/15' },
    { mes: 'MAR', h: 50, color: 'bg-navy/15' },
    { mes: 'ABR', h: 72, color: 'bg-sky' },
    { mes: 'MAI', h: 64, color: 'bg-navy/15' },
    { mes: 'JUN', h: 88, color: 'bg-ocean' },
    { mes: 'JUL', h: 100, color: 'bg-navy' },
];
</script>
