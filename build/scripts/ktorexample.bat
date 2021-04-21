@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  ktorexample startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and KTOREXAMPLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\ktorexample-0.0.1-SNAPSHOT.jar;%APP_HOME%\lib\ktor-server-tomcat-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-server-servlet-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-server-host-common-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-locations-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-auth-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-gson-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-serialization-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-jackson-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-server-core-jvm-1.5.3.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.4.32.jar;%APP_HOME%\lib\logback-classic-1.2.3.jar;%APP_HOME%\lib\logback-core-1.2.3.jar;%APP_HOME%\lib\kotlin-logging-1.7.8.jar;%APP_HOME%\lib\embedded-postgres-1.2.9.jar;%APP_HOME%\lib\ktor-client-gson-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-client-cio-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-network-tls-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-client-json-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-client-core-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-http-cio-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-http-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-network-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-utils-jvm-1.5.3.jar;%APP_HOME%\lib\ktor-io-jvm-1.5.3.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\ktorm-support-postgresql-3.3.0.jar;%APP_HOME%\lib\ktorm-core-3.3.0.jar;%APP_HOME%\lib\postgresql-42.2.14.jar;%APP_HOME%\lib\c3p0-0.9.5.5.jar;%APP_HOME%\lib\flyway-core-7.5.4.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.10.2.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.4.32.jar;%APP_HOME%\lib\kotlinx-serialization-json-jvm-1.1.0.jar;%APP_HOME%\lib\kotlinx-coroutines-jdk8-1.4.3-native-mt.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.4.3-native-mt.jar;%APP_HOME%\lib\jackson-module-kotlin-2.10.2.jar;%APP_HOME%\lib\kotlin-reflect-1.4.32.jar;%APP_HOME%\lib\kotlinx-serialization-core-jvm-1.1.0.jar;%APP_HOME%\lib\kotlin-stdlib-1.4.32.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.4.32.jar;%APP_HOME%\lib\embedded-postgres-binaries-windows-amd64-10.11.0-1.jar;%APP_HOME%\lib\embedded-postgres-binaries-darwin-amd64-10.11.0-1.jar;%APP_HOME%\lib\embedded-postgres-binaries-linux-amd64-10.11.0-1.jar;%APP_HOME%\lib\embedded-postgres-binaries-linux-amd64-alpine-10.11.0-1.jar;%APP_HOME%\lib\commons-lang3-3.10.jar;%APP_HOME%\lib\commons-compress-1.20.jar;%APP_HOME%\lib\xz-1.8.jar;%APP_HOME%\lib\commons-io-2.7.jar;%APP_HOME%\lib\commons-codec-1.14.jar;%APP_HOME%\lib\mchange-commons-java-0.2.19.jar;%APP_HOME%\lib\tomcat-catalina-9.0.37.jar;%APP_HOME%\lib\tomcat-embed-core-9.0.37.jar;%APP_HOME%\lib\gson-2.8.6.jar;%APP_HOME%\lib\jackson-databind-2.10.2.jar;%APP_HOME%\lib\jackson-annotations-2.10.2.jar;%APP_HOME%\lib\jackson-core-2.10.2.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\tomcat-jsp-api-9.0.37.jar;%APP_HOME%\lib\tomcat-util-scan-9.0.37.jar;%APP_HOME%\lib\tomcat-api-9.0.37.jar;%APP_HOME%\lib\tomcat-coyote-9.0.37.jar;%APP_HOME%\lib\tomcat-servlet-api-9.0.37.jar;%APP_HOME%\lib\tomcat-util-9.0.37.jar;%APP_HOME%\lib\tomcat-juli-9.0.37.jar;%APP_HOME%\lib\tomcat-annotations-api-9.0.37.jar;%APP_HOME%\lib\tomcat-jni-9.0.37.jar;%APP_HOME%\lib\tomcat-jaspic-api-9.0.37.jar;%APP_HOME%\lib\tomcat-el-api-9.0.37.jar;%APP_HOME%\lib\json-simple-1.1.1.jar;%APP_HOME%\lib\config-1.3.1.jar


@rem Execute ktorexample
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %KTOREXAMPLE_OPTS%  -classpath "%CLASSPATH%" smn.ktorexample.MainKt %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable KTOREXAMPLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%KTOREXAMPLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
