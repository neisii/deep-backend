# 이 프로젝트를 이미지 화 하기 위한 스크립트
FROM openjdk:17-jdk
LABEL authors="NEISII"

WORKDIR /app

COPY build/libs/backendProject-0.0.1-SNAPSHOT.jar /app/backendProject-0.0.1-SNAPSHOT.jar


ENV PROJECT_NAME ="스프링 웹 서버입니다."

CMD ["java", "-jar", "/app/backendProject-0.0.1-SNAPSHOT.jar"]