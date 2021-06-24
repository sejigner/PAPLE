# Closest
## Git Flow를 활용한 브랜치 전략

1. Github Issue에 이슈 생성
2. $ git flow feature start <이슈 번호>-<이슈 제목>
3. 작업 후 생성된 파일들 stage에 올리기 ($ git add )
4. $ git commit -m "명령문 형태의 제목" -m "어떻게 보다는 무엇을, 왜"
5. $ git flow feature finish <이슈번호>-<이슈 제목>
6. git push origin develop
