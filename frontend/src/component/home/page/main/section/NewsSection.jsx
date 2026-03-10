import { useState, useEffect } from "react";
import axios from "axios";
import { Card, SectionText } from "../MainPageUi";

export default function NewsSection() {
  const [news, setNews] = useState([]);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getDate().toString().padStart(2, '0')}`;
  };

  useEffect(() => {
    const getNews = async () => {
      try {
        const res = await axios.get("/api/news/open");
        if (res.data?.content?.items) setNews(res.data.content.items);
      } catch (err) {
        console.error("뉴스 로드 실패", err);
      }
    };
    getNews();
  }, []);

  return (
    <section className="space-y-4">
      <SectionText title="지역별 주요 농업 뉴스" />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {news.length > 0 ? (
          news.slice(0, 3).map((item, index) => (
            <Card
              key={index}
              className="p-5 rounded-2xl border-slate-100 hover:shadow-md transition-shadow cursor-pointer"
              onClick={() => window.open(item.originallink, "_blank")}
            >
              <div className="flex flex-col h-full justify-between space-y-3">
                <h2
                  className="font-bold text-base leading-snug line-clamp-2"
                  dangerouslySetInnerHTML={{ __html: item.title }}
                />
                <p className="text-xs text-slate-400">{formatDate(item.pubDate)}</p>
              </div>
            </Card>
          ))
        ) : (
          <div className="col-span-3 text-center py-10 text-slate-400">뉴스를 불러오는 중입니다...</div>
        )}
      </div>
    </section>
  );
}