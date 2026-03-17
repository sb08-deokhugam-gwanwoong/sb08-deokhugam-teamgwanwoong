FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0

# 1. 혹시 남아있을지 모르는 고장 난 찌꺼기 폴더를 강제 삭제합니다.
RUN rm -rf /usr/share/elasticsearch/plugins/analysis-nori

# 2. 플러그인을 새롭게 깔끔하게 설치합니다.
RUN bin/elasticsearch-plugin install analysis-nori