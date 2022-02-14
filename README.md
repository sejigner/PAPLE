
# PAPLE

Version 6 (1.0.5)

Release Date #22.02.05

Update Date #22.02.14

## Git Flow를 활용한 브랜치 관리

1. Github Issue에 이슈 생성
2. $ git flow feature start <이슈 번호>-<이슈 제목>
3. 작업 후 생성된 파일들 stage에 올리기 ($ git add )
4. $ git commit -m "명령문 형태의 제목" -m "어떻게 보다는 무엇을, 왜"
5. $ git flow feature finish <이슈번호>-<이슈 제목>
6. $ git push origin develop

## 새로운 환경에서 원격저장소 초기 연동하기
1. Git 설치
2. $ git config --global user.name "이름"
3. $ git config --global user.email "이메일"
4. git bash 실행 후 cd /디렉토리 명령어로 원하는 로컬저장소로 이동
5. $ git init으로 git 생성
6. $ git remote add origin 깃 url
7. $ git pull origin develop(또는 원하는 Git Branch) 
8. $ git flow init (최초 1회)
9. Git Flow 활용 브랜치 관리 방법 따르기

