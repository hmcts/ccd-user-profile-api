--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.19
-- Dumped by pg_dump version 13.1

--
-- Name: action_crud; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.action_crud AS ENUM (
    'READ',
    'CREATE',
    'UPDATE',
    'DELETE'
);


SET default_tablespace = '';

--
-- Name: jurisdiction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jurisdiction (
    id character varying(64) NOT NULL
);


--
-- Name: user_profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_profile (
    id character varying(254) NOT NULL,
    work_basket_default_jurisdiction character varying(64),
    work_basket_default_case_type character varying(255),
    work_basket_default_state character varying(255)
);


--
-- Name: user_profile_audit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_profile_audit (
    id integer NOT NULL,
    jurisdiction_id character varying(64) NOT NULL,
    user_profile_id character varying(64) NOT NULL,
    work_basket_default_jurisdiction character varying(64),
    work_basket_default_case_type character varying(255),
    work_basket_default_state character varying(255),
    "timestamp" timestamp without time zone DEFAULT now() NOT NULL,
    action public.action_crud NOT NULL,
    actioned_by text NOT NULL
);


--
-- Name: user_profile_audit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_profile_audit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_profile_audit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_profile_audit_id_seq OWNED BY public.user_profile_audit.id;


--
-- Name: user_profile_jurisdiction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_profile_jurisdiction (
    user_profile_id character varying(254) NOT NULL,
    jurisdiction_id character varying(64) NOT NULL
);


--
-- Name: user_profile_audit id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile_audit ALTER COLUMN id SET DEFAULT nextval('public.user_profile_audit_id_seq'::regclass);




--
-- Name: jurisdiction jurisdiction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jurisdiction
    ADD CONSTRAINT jurisdiction_pkey PRIMARY KEY (id);


--
-- Name: user_profile_audit user_profile_audit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile_audit
    ADD CONSTRAINT user_profile_audit_pkey PRIMARY KEY (id);


--
-- Name: user_profile user_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT user_profile_pkey PRIMARY KEY (id);


--
-- Name: user_profile_audit fk_user_profile__audit_jurisdiction; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile_audit
    ADD CONSTRAINT fk_user_profile__audit_jurisdiction FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: user_profile_jurisdiction fk_user_profile_jurisidction_jurisdiction; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile_jurisdiction
    ADD CONSTRAINT fk_user_profile_jurisidction_jurisdiction FOREIGN KEY (user_profile_id) REFERENCES public.user_profile(id);


--
-- Name: user_profile_jurisdiction fk_user_profile_jurisidction_user_profile; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile_jurisdiction
    ADD CONSTRAINT fk_user_profile_jurisidction_user_profile FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: user_profile fk_user_profile_work_basket_default_jurisidction; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT fk_user_profile_work_basket_default_jurisidction FOREIGN KEY (work_basket_default_jurisdiction) REFERENCES public.jurisdiction(id);


--
-- PostgreSQL database dump complete
--

