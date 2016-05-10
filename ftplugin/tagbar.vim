" KeY tags definitions

if !exists(':Tagbar')
    finish
endif

let g:tagbar_type_key = {
    \ 'ctagstype' : 'key',
    \ 'kinds' : [
        \ 's:Symbols',
        \ 'v:Schema Variables',
        \ 'r:Rule Scopes'
    \ ],
    \ 'deffile' : expand('<sfile>:p:h:h') . '/ctags/key.ctags',
    \ 'sort' : 0
\ }
