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