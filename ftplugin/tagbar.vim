" KeY tags definitions

if !exists(':Tagbar')
    finish
endif

let g:tagbar_type_key = {
    \ 'ctagsbin' : expand('<sfile>:p:h:h') . '/ctags/KeYTags.jar',
    \ 'ctagsargs' : '',
    \ 'kinds' : [
        \ 's:Sorts',
        \ 'f:Functions',
        \ 'p:Predicates',
        \ 'o:Program Variables',
        \ 'v:Schema Variables',
        \ 'c:Rule Scopes',
        \ 'r:Rules',
    \ ],
    \ 'sort' : 1,
    \ 'sro' : ' : '
\ }
