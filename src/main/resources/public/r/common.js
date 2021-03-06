'use strict';

Vue.component('login-button', {
    template: '<a class="btn btn-secondary" href="/p/login.html" v-if="!isLoggedIn"><i class="fa fa-sign-in" aria-hidden="true"></i> Login</a>' +
              '<a class="btn btn-secondary" href="#" :title="welcome" @click.prevent="logout" v-else>Logout <i class="fa fa-sign-out" aria-hidden="true"></i></a>',
    props: {
        loginRequired: {
            type: Boolean,
            default: false
        },
        redirect: {
            type: String
        }
    },
    data: function () {
        return {
            sessionToken: '',
            userId: 0,
            backend: {}
        }
    },
    computed: {
      isLoggedIn: function () {
          return this.sessionToken && this.sessionToken.length > 0;
      },
      welcome: function () {
          return 'You are logged in as ' + this.userId;
      }
    },
    methods: {
        logout: function () {
            localStorage.removeItem('session-token');
            sessionStorage.removeItem('session-token');
            localStorage.removeItem('user-id');
            sessionStorage.removeItem('user-id');

            this.backend.delete('sessions/' + this.sessionToken)
            .then(() => {
                if (this.redirect) {
                    window.location.replace(this.redirect);
                } else {
                    window.location.reload();
                }
            });
        }
    },
    mounted: function () {
        this.sessionToken = sessionStorage.getItem('session-token') || localStorage.getItem('session-token');
        this.userId = sessionStorage.getItem('user-id') || localStorage.getItem('user-id');

        this.backend = axios.create({
            baseURL: '${env.HEROKU_URL}/api/v1/',
            headers: { 'Content-Type': 'application/json; charset=utf-8' }
        });

        if ( this.loginRequired && ! this.sessionToken ) {
            window.location.replace('${env.HEROKU_URL}/p/login.html');
        } else {
            this.backend.defaults.headers.common['Authorization'] = this.sessionToken;

            this.$emit('user-identified', this.userId, this.backend);
        }
    }
});

Vue.component('toggle-button', {
    template:   '<div class="btn-group" role="group">' +
                    '<button type="button" class="btn" :class="leftClass" @click="setLeft">{{labels.left}}</button>' +
                    '<button type="button" class="btn" :class="rightClass" @click="setRight">{{labels.right}}</button>' +
                '</div>',
    props: {
        value: Object,
        values: {
            type: Object,
            required: true,
        },
        labels: {
            type: Object,
            required: true
        }
    },
    methods: {
        setLeft: function() {
            this.setValue(this.values.left);
        },
        setRight: function() {
            this.setValue(this.values.right);
        },
        setValue: function (value) {
            this.value = value;
            this.$emit('input', this.value);
            this.$emit('change');
        }
    },
    computed: {
        leftClass: function() {
            return {
                'btn-primary': this.value === this.values.left,
                'btn-secondary': this.value !== this.values.left
            }
        },
        rightClass: function() {
            return {
                'btn-primary': this.value === this.values.right,
                'btn-secondary': this.value !== this.values.right
            }
        }
    }
});

Vue.component('nav-bar', {
    template:   '<nav>' +
                    '<ul class="nav nav-pills float-right">' +
                        '<li class="nav-item" v-for="link in links" v-if="!link.userLink || user > 0">' +
                            '<a class="nav-link" :class="{active: active === link.name}" :href="link.href">{{link.name}}</a>' +
                        '</li>' +
                        '<li class="nav-item">' +
                            '<slot>' +
                                '<a class="nav-link" href="/p/login.html">Login</span></a>' +
                            '</slot>' +
                        '</li>' +
                    '</ul>' +
                '</nav>',
    props: {
        active: {
            type: String
        },
        user: {
            type: Number
        }
    },
    computed: {
        links: function() {
            return  [
                { name: 'Profile', href: '/p/profile/' + this.user },
                { name: 'Options', href: '/u/options', userLink: true },
                { name: 'Manage', href: '/u/manage' },
                { name: 'About', href: '/p/about' },
                { name: 'Contact', href: '/p/about#contact-us' }
            ]
        }
    }
});