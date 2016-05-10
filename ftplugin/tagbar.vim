" KeY tags definitions

if !exists(':Tagbar')
    finish
endif

let g:tagbar_type_key = {
    \ 'ctagstype' : 'key',
    \ 'kinds' : [
        \ 'p:predicates',
        \ 'f:functions',
        \ 'r:rules',
        \ 's:sorts',
        \ 'v:schemavariables'
    \ ],
    \ 'deffile' : expand('<sfile>:p:h:h') . '/ctags/key.ctags'
\ }
