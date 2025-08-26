FROM amazoncorretto:21-alpine

WORKDIR /deploy

COPY build/libs/*.jar app.jar

RUN apk add tzdata && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENV TZ=Asia/Seoul

CMD ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/deploy/app.jar", "--spring.profiles.active=prod"]

EXPOSE 8080