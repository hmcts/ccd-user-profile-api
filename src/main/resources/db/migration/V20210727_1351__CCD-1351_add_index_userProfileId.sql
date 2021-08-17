CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_profile_jurisdiction_user_profile_id ON public.user_profile_jurisdiction USING btree (user_profile_id);
