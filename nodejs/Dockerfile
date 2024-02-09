FROM hypi/fenrir-runtime-nodejs:v1

ADD src/main.js /home/hypi/fenrir/function/
#Any dependencies declared in package.json will be installed and available to the function
ADD package.json /home/hypi/fenrir/function/
RUN /home/hypi/fenrir/build.sh
