" KeY tags definitions

if !exists(':Tagbar')
    finish
endif

let g:tagbar_type_key = {
    \ 'ctagstype' : 'key',
    \ 'kinds' : [
        \ 's:sorts',
        \ 'p:predicates',
        \ 'f:functions',
        \ 'v:schemavariables',
        \ 'r:rules'
    \ ],
    \ 'deffile' : expand('<sfile>:p:h:h') . '/ctags/key.ctags',
    \ 'sort' : 0
\ }
