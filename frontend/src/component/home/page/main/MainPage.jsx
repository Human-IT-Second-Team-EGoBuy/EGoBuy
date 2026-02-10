import { useState } from "react";
import { Card, WeatherUi, SectionText } from "./MainPageUi";

export default function MainPage() {
  const [news, setnews] = useState();

  return (
    <div className="p-4 md:p-6 space-y-8">
      <WeatherUi>
        <div>
          <h2 className="text-2xl font-bold">weather text</h2>
          <p className="text-sm opacity-80 mt-2">여기에 날씨 요약/한줄 피드백</p>
        </div>
      </WeatherUi>

      <section className="space-y-4">
        <SectionText title="지역별 주요 농업 뉴스"/>
        

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="p-4 rounded-2xl border-slate-100">
            <h2>news</h2>
          </Card>
          <Card className="p-4 rounded-2xl border-slate-100">
            <h2>news</h2>
          </Card>
          <Card className="p-4 rounded-2xl border-slate-100">
            <h2>news</h2>
          </Card>
        </div>
      </section>

      <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <h2 className="font-bold text-lg mb-4">diagnosis ai</h2>
          <div className="text-sm text-slate-500">여기에 병충해 진단 ai UI</div>
        </Card>

        <Card>
          <h2 className="font-bold text-lg mb-4">farming guide search</h2>
          <div className="text-sm text-slate-500">여기에 검색 UI</div>
        </Card>
      </section>

      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6 pb-20">
        <Card className="lg:col-span-1">
          <h2 className="font-bold text-lg mb-4">retail graph</h2>
          <div className="text-sm text-slate-500">여기에 그래프</div>
        </Card>

        <Card className="lg:col-span-2">
          <h2 className="font-bold text-lg mb-4">community show</h2>
          <div className="text-sm text-slate-500">여기에 커뮤니티 리스트</div>
        </Card>
      </section>
    </div>
  );
}